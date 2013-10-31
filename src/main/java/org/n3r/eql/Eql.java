package org.n3r.eql;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.n3r.eql.config.Configable;
import org.n3r.eql.config.EqlConfigManager;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.impl.DbTypeFactory;
import org.n3r.eql.impl.EqlBatch;
import org.n3r.eql.impl.EqlRsRetriever;
import org.n3r.eql.impl.SqlResourceLoader;
import org.n3r.eql.map.EqlRowMapper;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.param.EqlParamPlaceholder;
import org.n3r.eql.param.EqlParamsBinder;
import org.n3r.eql.param.EqlParamsParser;
import org.n3r.eql.parser.DynamicReplacer;
import org.n3r.eql.parser.SqlBlock;
import org.n3r.eql.util.EqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Eql implements Closeable {
    public static final String DEFAULT_CONN_NAME = "DEFAULT";
    private Logger logger = LoggerFactory.getLogger(Eql.class);
    private Object connectionNameOrConfigable;
    private SqlBlock sqlBlock;
    private Object[] params;
    private String sqlClassPath;
    private EqlPage page;
    private EqlBatch batch;
    private Object[] dynamics;
    private EqlTran externalTran;
    private EqlTran internalTran;
    private Connection connection;
    private ArrayList<Object> executeResults;
    private DbType dbType;
    private EqlRsRetriever rsRetriever = new EqlRsRetriever();
    private int fetchSize;
    private String currentSql;

    public Connection getConnection() {
        return newTran(connectionNameOrConfigable).getConn();
    }

    private void createConn() {
        if (connection != null) return;

        connection = internalTran != null ? internalTran.getConn() : externalTran.getConn();
        dbType = DbTypeFactory.parseDbType(connection);
    }

    public List<Object> getResults() {
        return executeResults;
    }

    @SuppressWarnings("unchecked")
    public <T> T execute(String... directSqls) {
        checkPreconditions(directSqls);
        executeResults = new ArrayList<Object>();

        Object ret = null;
        try {
            if (batch == null) tranStart();
            createConn();

            for (EqlRun subSql : createSqlSubs(directSqls)) {
                ret = execSub(ret, subSql);
                executeResults.add(ret);
            }

            if (batch == null) tranCommit();
        } catch (SQLException e) {
            logger.error("sql exception", e);
            if (batch != null) batch.cleanupBatch();
            throw new EqlExecuteException("exec sql failed[" + currentSql + "]" + e.getMessage());
        } finally {
            resetState();
            close();
        }

        return (T) ret;
    }

    private void resetState() {
        rsRetriever.resetMaxRows();
    }

    @Override
    public void close() {
        if (batch == null) tranClose();
        connection = null;
    }

    public ESelectStmt selectStmt() {
        tranStart();
        createConn();

        List<EqlRun> sqlSubs = createSqlSubs();
        if (sqlSubs.size() != 1)
            throw new EqlExecuteException("only one select sql supported in this method");

        EqlRun sqlSub = sqlSubs.get(0);
        if (sqlSub.getSqlType() != EqlRun.EqlType.SELECT)
            throw new EqlExecuteException("only one select sql supported in this method");

        ESelectStmt selectStmt = new ESelectStmt();
        try {
            prepareStmt(selectStmt, sqlSub);
        } catch (SQLException e) {
            throw new EqlExecuteException("prepareSelectStmt fail", e);
        }
        selectStmt.setRsRetriever(rsRetriever);
        selectStmt.setFetchSize(fetchSize);

        return selectStmt;
    }

    public EUpdateStmt updateStmt() {
        tranStart();
        createConn();

        List<EqlRun> sqlSubs = createSqlSubs();
        if (sqlSubs.size() != 1)
            throw new EqlExecuteException("only one update sql supported in this method");

        EqlRun sqlSub = sqlSubs.get(0);
        if (!EqlUtils.isUpdateStmt(sqlSub))
            throw new EqlExecuteException("only one update/merge/delete/insert sql supported in this method");

        EUpdateStmt updateStmt = new EUpdateStmt();
        try {
            prepareStmt(updateStmt, sqlSub);
        } catch (SQLException e) {
            throw new EqlExecuteException("prepareSelectStmt fail", e);
        }

        return updateStmt;
    }


    private void checkPreconditions(String... directSqls) {
        if (sqlBlock != null || directSqls.length > 0) return;

        throw new EqlExecuteException("No sqlid defined!");
    }

    private Object execSub(Object ret, EqlRun esqlRun) throws SQLException {
        currentSql = esqlRun.getSql();

        try {
            return EqlUtils.isDdl(esqlRun) ? execDdl(createRealSql(esqlRun)) : pageExecute(ret, esqlRun);
        } catch (EqlExecuteException ex) {
            if (!esqlRun.getSqlBlock().isOnerrResume()) throw ex;
        }

        return 0;
    }

    private boolean execDdl(String sql) {
        Statement stmt = null;
        logger.debug("ddl sql {}: {}", getSqlId(), sql);
        try {
            stmt = connection.createStatement();
            return stmt.execute(sql);
        } catch (SQLException ex) {
            throw new EqlExecuteException(ex);
        } finally {
            EqlUtils.closeQuietly(stmt);
        }
    }

    private Object pageExecute(Object ret, EqlRun subSql) throws SQLException {
        if (page == null || !subSql.isLastSelectSql()) return execDml(ret, subSql);

        if (page.getTotalRows() == 0) page.setTotalRows(executeTotalRowsSql(ret, subSql));

        return executePageSql(ret, subSql);
    }

    private Object executePageSql(Object ret, EqlRun eqlRun) throws SQLException {
        // oracle physical pagination
        EqlRun pageSql = dbType.createPageSql(eqlRun, page);

        return execDml(ret, pageSql);
    }

    private int executeTotalRowsSql(Object ret, EqlRun subSql) throws SQLException {
        EqlRun totalSqlSub = subSql.clone();
        createTotalSql(totalSqlSub);

        Object totalRows = execDml(ret, totalSqlSub);
        if (totalRows instanceof Number) return ((Number) totalRows).intValue();

        throw new EqlExecuteException("returned total rows object " + totalRows + " is not a number");
    }

    private void createTotalSql(EqlRun subSql) {
        String sql = subSql.getSql().toUpperCase();
        int fromPos1 = sql.indexOf("FROM");

        int fromPos2 = sql.indexOf("DISTINCT");
        fromPos2 = fromPos2 < 0 ? sql.indexOf("FROM", fromPos1 + 4) : fromPos2;

        subSql.setSql(fromPos2 > 0 ? "SELECT COUNT(*) CNT__ FROM (" + sql + ")"
                : "SELECT COUNT(*) AS CNT " + sql.substring(fromPos1));
        subSql.setWillReturnOnlyOneRow(true);
    }

    private Object execDml(Object ret, EqlRun subSql) throws SQLException {
        Object execRet = batch != null ? execDmlInBatch(ret, subSql) : execDmlNoBatch(ret, subSql);
        logger.debug("result {}: {}", getSqlId(), execRet);

        return execRet;
    }

    private Object execDmlInBatch(Object ret, EqlRun eqlRun) throws SQLException {
        return batch.processBatchUpdate(eqlRun);
    }

    private void prepareStmt(EStmt stmt, EqlRun eqlRun) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = prepareSql(eqlRun, createRealSql(eqlRun));

            stmt.setPreparedStatment(ps);
            stmt.setEqlRun(eqlRun);
            stmt.setLogger(logger);
            stmt.setParams(params);
            stmt.setEqlTran(externalTran != null ? externalTran : internalTran);
        } catch (Exception ex) {
            EqlUtils.closeQuietly(ps);
        }
    }

    private Object execDmlNoBatch(Object ret, EqlRun eqlRun) throws SQLException {
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = prepareSql(eqlRun, createRealSql(eqlRun));
            new EqlParamsBinder().bindParams(ps, eqlRun, params, logger);

            if (eqlRun.getSqlType() == EqlRun.EqlType.SELECT) {
                rs = ps.executeQuery();
                if (fetchSize > 0) rs.setFetchSize(fetchSize);

                return rsRetriever.convert(rs, eqlRun);
            }

            if (EqlUtils.isProcedure(eqlRun.getSqlType())) {
                CallableStatement cs = (CallableStatement) ps;
                return execAndRetrieveProcedureRet(eqlRun, cs);
            }

            return ps.executeUpdate();

        } finally {
            EqlUtils.closeQuietly(rs, ps);
        }
    }

    private Object execAndRetrieveProcedureRet(EqlRun subSql, CallableStatement cs) throws SQLException {
        cs.execute();

        if (subSql.getOutCount() == 0) return null;

        if (subSql.getOutCount() == 1)
            for (int i = 0, ii = subSql.getPlaceHolders().length; i < ii; ++i)
                if (subSql.getPlaceHolders()[i].getInOut() != EqlParamPlaceholder.InOut.IN) return cs.getObject(i + 1);

        switch (subSql.getPlaceHolderOutType()) {
            case AUTO_SEQ:
                return retrieveAutoSeqOuts(subSql, cs);
            case VAR_NAME:
                return rsRetriever.getCallableReturnMapper().mapResult(subSql, cs);
            default:
                break;
        }

        return null;
    }

    private Object retrieveAutoSeqOuts(EqlRun subSql, CallableStatement cs) throws SQLException {
        List<Object> objects = Lists.newArrayList();
        for (int i = 0, ii = subSql.getPlaceHolders().length; i < ii; ++i)
            if (subSql.getPlaceHolders()[i].getInOut() != EqlParamPlaceholder.InOut.IN)
                objects.add(cs.getObject(i + 1));

        return objects;
    }

    public PreparedStatement prepareSql(EqlRun subSql, String realSql) throws SQLException {
        logger.debug("prepare sql {}: {} ", getSqlId(), realSql);
        return EqlUtils.isProcedure(subSql.getSqlType())
                ? connection.prepareCall(realSql) : connection.prepareStatement(realSql);
    }

    public String createRealSql(EqlRun eqlRun) {
        String sql = new DynamicReplacer().repaceDynamics(eqlRun, dynamics);

        return EqlUtils.autoTrimLastUnusedPart(sql);
    }

    public Eql returnType(Class<?> returnType) {
        rsRetriever.setReturnType(returnType);
        return this;
    }

    public Eql returnType(EqlRowMapper eqlRowMapper) {
        rsRetriever.setEqlRowMapper(eqlRowMapper);
        return this;
    }

    public Eql(String connectionName) {
        this.connectionNameOrConfigable = connectionName;
    }

    public Eql(Configable configable) {
        this.connectionNameOrConfigable = configable;
    }

    public Eql() {
        this(DEFAULT_CONN_NAME);
    }

    protected void initSqlId(String sqlId, String sqlClassPath) {
        this.sqlClassPath = Strings.isNullOrEmpty(sqlClassPath) ? EqlUtils.getSqlClassPath(4) : sqlClassPath;

        sqlBlock = SqlResourceLoader.load(this.sqlClassPath, sqlId);

        rsRetriever.setSqlBlock(sqlBlock);
    }

    protected List<EqlRun> createSqlSubs(String... directSqls) {
        return directSqls.length == 0
                ? sqlBlock.createSqlSubs(params)
                : createSqlSubsByDirectSqls(directSqls);
    }

    private List<EqlRun> createSqlSubsByDirectSqls(String[] sqls) {
        List<EqlRun> sqlSubs = Lists.newArrayList();
        EqlRun lastSelectSql = null;
        for (String sql : sqls) {
            EqlRun subSql = new EqlParamsParser().parseParams(sql, null);
            sqlSubs.add(subSql);

            if (subSql.getSqlType() == EqlRun.EqlType.SELECT) lastSelectSql = subSql;
        }

        if (lastSelectSql != null) lastSelectSql.setLastSelectSql(true);

        return sqlSubs;
    }

    public Eql useSqlFile(Class<?> sqlBoundClass) {
        sqlClassPath = sqlBoundClass.getName().replace('.', '/') + ".eql";
        return this;
    }

    public Eql useSqlFile(String sqlClassPath) {
        this.sqlClassPath = sqlClassPath;
        return this;
    }

    public Eql id(String sqlId) {
        initSqlId(sqlId, sqlClassPath);
        return this;
    }

    public Eql merge(String sqlId) {
        initSqlId(sqlId, sqlClassPath);
        return this;
    }

    public Eql update(String sqlId) {
        initSqlId(sqlId, sqlClassPath);
        return this;
    }

    public Eql insert(String sqlId) {
        initSqlId(sqlId, sqlClassPath);
        return this;
    }

    public Eql delete(String sqlId) {
        initSqlId(sqlId, sqlClassPath);
        return this;
    }

    public Eql select(String sqlId) {
        initSqlId(sqlId, sqlClassPath);
        return this;
    }

    public Eql selectFirst(String sqlId) {
        initSqlId(sqlId, sqlClassPath);
        limit(1);
        return this;
    }

    public Eql procedure(String sqlId) {
        initSqlId(sqlId, sqlClassPath);
        return this;
    }

    public String getSqlId() {
        return sqlBlock.getSqlId();
    }

    private void tranStart() {
        if (externalTran != null) return;
        if (internalTran != null) return;

        internalTran = newTran(connectionNameOrConfigable);
        internalTran.start();
    }

    private void tranCommit() {
        if (externalTran != null) return;

        internalTran.commit();
    }

    private void tranClose() {
        if (internalTran != null) EqlUtils.closeQuietly(internalTran);

        internalTran = null;
    }

    public EqlTran newTran() {
        EqlTran tran = newTran(connectionNameOrConfigable);
        connection = tran.getConn();
        useTran(tran);

        return tran;
    }

    public Eql useTran(EqlTran tran) {
        externalTran = tran;
        return this;
    }

    public static EqlTran newTran(Object connectionNameOrConfigable) {
        return EqlConfigManager.getConfig(connectionNameOrConfigable).getTran();
    }

    public String getConnectionName() {
        return "" + connectionNameOrConfigable;
    }

    public String getSqlPath() {
        return sqlClassPath;
    }

    public Eql params(Object... params) {
        this.params = params;
        this.dynamics = params;
        return this;
    }

    public Eql limit(EqlPage page) {
        this.page = page;

        return this;
    }

    public Eql startBatch(int maxBatches) {
        batch.startBatch(maxBatches);
        return this;
    }

    public Eql startBatch() {
        batch = new EqlBatch(this);
        batch.startBatch();

        tranStart();
        return this;
    }

    public int executeBatch() {
        int totalRows = 0;
        try {
            totalRows = batch.processBatchExecution();
            tranCommit();
        } catch (SQLException e) {
            throw new EqlExecuteException("executeBatch failed:" + e.getMessage());
        } finally {
            tranClose();
        }

        batch = null;
        return totalRows;
    }

    public Eql dynamics(Object... dynamics) {
        this.dynamics = dynamics;
        return this;
    }

    public Eql limit(int maxRows) {
        rsRetriever.setMaxRows(maxRows);
        return this;
    }

    public Object[] getParams() {
        return params;
    }

    public Logger getLogger() {
        return logger;
    }

    public Eql returnType(String returnTypeName) {
        rsRetriever.setReturnTypeName(returnTypeName);
        return this;
    }

    public Eql setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
        return this;
    }
}
