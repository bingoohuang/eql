package org.n3r.eql;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.n3r.eql.config.EqlConfigManager;
import org.n3r.eql.config.EqlConfigable;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.impl.*;
import org.n3r.eql.map.EqlRowMapper;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.param.EqlParamsBinder;
import org.n3r.eql.parser.EqlBlock;
import org.n3r.eql.util.EqlUtils;
import org.n3r.eql.util.HostAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.sql.*;
import java.util.List;
import java.util.Map;

public class Eql implements Closeable {
    public static final String DEFAULT_CONN_NAME = "DEFAULT";
    private static Logger logger = LoggerFactory.getLogger(Eql.class);

    private Object connectionNameOrConfigable;
    private EqlBlock eqlBlock;
    private Object[] params;
    private String sqlClassPath;
    private EqlPage page;
    private EqlBatch batch;
    private Object[] dynamics;
    private EqlTran externalTran;
    private EqlTran internalTran;
    private Connection connection;
    private DbType dbType;
    private EqlRsRetriever rsRetriever = new EqlRsRetriever();
    private int fetchSize;
    private List<EqlRun> eqlRuns;
    private EqlRun currRun;
    private Map<String, Object> executionContext;

    public Connection getConnection() {
        return newTran(connectionNameOrConfigable).getConn();
    }

    private void createConn() {
        if (connection != null) return;

        connection = internalTran != null ? internalTran.getConn() : externalTran.getConn();
        dbType = DbType.parseDbType(connection);
        executionContext.put("_databaseId", dbType.getDatabaseId());
    }

    @SuppressWarnings("unchecked")
    public <T> T execute(String... directSqls) {
        checkPreconditions(directSqls);

        newExecutionContext();
        Object ret = null;
        try {
            if (batch == null) tranStart();
            createConn();

            eqlRuns = eqlBlock.createEqlRuns(executionContext, params, dynamics, directSqls);
            for (EqlRun eqlRun : eqlRuns) {
                currRun = eqlRun;
                ret = runEql();
                updateLastResultToExecutionContext(ret);
                currRun.setResult(ret);
            }

            if (batch == null) tranCommit();
        } catch (SQLException e) {
            logger.error("sql exception", e);
            if (batch != null) batch.cleanupBatch();
            throw new EqlExecuteException("exec sql failed["
                    + currRun.getPrintSql() + "]" + e.getMessage());
        } finally {
            resetState();
            close();
        }

        return (T) ret;
    }

    private void updateLastResultToExecutionContext(Object ret) {
        Object lastResult = ret;
        if (ret instanceof List) {
            List list = (List)ret;
            if (list.size() == 0) lastResult = null;
            else if (list.size() == 1) lastResult = list.get(0);
        }

        executionContext.put("_lastResult", lastResult);
    }

    private void newExecutionContext() {
        executionContext = Maps.newHashMap();
        executionContext.put("_time", new Timestamp(System.currentTimeMillis()));
        executionContext.put("_date", new java.util.Date());
        executionContext.put("_host", HostAddress.getHost());
        executionContext.put("_ip", HostAddress.getIp());
        executionContext.put("_results", Lists.newArrayList());
        executionContext.put("_lastResult", "");
        executionContext.put("_params", params);
        executionContext.put("_dynamics", dynamics);
    }

    public List<EqlRun> getEqlRuns() {
        return eqlRuns;
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

        List<EqlRun> sqlSubs = eqlBlock.createEqlRunsByEqls(executionContext, params, dynamics);
        if (sqlSubs.size() != 1)
            throw new EqlExecuteException("only one select sql supported in this method");

        currRun = sqlSubs.get(0);
        if (currRun.getSqlType() != EqlRun.EqlType.SELECT)
            throw new EqlExecuteException("only one select sql supported in this method");

        ESelectStmt selectStmt = new ESelectStmt();
        try {
            prepareStmt(selectStmt);
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

        List<EqlRun> sqlSubs = eqlBlock.createEqlRunsByEqls(executionContext, params, dynamics);
        if (sqlSubs.size() != 1)
            throw new EqlExecuteException("only one update sql supported in this method");

        currRun = sqlSubs.get(0);
        if (!EqlUtils.isUpdateStmt(currRun))
            throw new EqlExecuteException("only one update/merge/delete/insert sql supported in this method");

        EUpdateStmt updateStmt = new EUpdateStmt();
        try {
            prepareStmt(updateStmt);
        } catch (SQLException e) {
            throw new EqlExecuteException("prepareSelectStmt fail", e);
        }

        return updateStmt;
    }

    private void checkPreconditions(String... directSqls) {
        if (eqlBlock != null || directSqls.length > 0) return;

        throw new EqlExecuteException("No sqlid defined!");
    }

    private Object runEql() throws SQLException {
        try {
            return EqlUtils.isDdl(currRun) ? execDdl() : pageExecute();
        } catch (EqlExecuteException ex) {
            if (!currRun.getEqlBlock().isOnerrResume()) throw ex;
        }

        return 0;
    }

    private boolean execDdl() {
        Statement stmt = null;
        logger.debug("ddl sql {}: {}", getSqlId(), currRun.getPrintSql());
        try {
            stmt = connection.createStatement();
            return stmt.execute(currRun.getRunSql());
        } catch (SQLException ex) {
            throw new EqlExecuteException(ex);
        } finally {
            EqlUtils.closeQuietly(stmt);
        }
    }

    private Object pageExecute() throws SQLException {
        if (page == null || !currRun.isLastSelectSql()) return execDml();

        if (page.getTotalRows() == 0) page.setTotalRows(executeTotalRowsSql());

        return executePageSql();
    }

    private Object executePageSql() throws SQLException {
        EqlRun temp = currRun;
        currRun = dbType.createPageSql(currRun, page);

        Object o = execDml();
        currRun = temp;

        return o;
    }

    private int executeTotalRowsSql() throws SQLException {
        EqlRun temp = currRun;
        currRun = createTotalSql();
        Object totalRows = execDml();
        currRun = temp;

        if (totalRows instanceof Number) return ((Number) totalRows).intValue();

        throw new EqlExecuteException("returned total rows object " + totalRows + " is not a number");
    }

    private EqlRun createTotalSql() {
        EqlRun totalEqlSql = currRun.clone();
        String sql = totalEqlSql.getRunSql().toUpperCase();
        int fromPos1 = sql.indexOf("FROM");

        int fromPos2 = sql.indexOf("DISTINCT");
        fromPos2 = fromPos2 < 0 ? sql.indexOf("FROM", fromPos1 + 4) : fromPos2;

        totalEqlSql.setRunSql(fromPos2 > 0 ? "SELECT COUNT(*) CNT__ FROM (" + sql + ")"
                : "SELECT COUNT(*) AS CNT " + sql.substring(fromPos1));
        totalEqlSql.setWillReturnOnlyOneRow(true);
        return totalEqlSql;
    }

    private Object execDml() throws SQLException {
        Object execRet = batch != null ? execDmlInBatch() : execDmlNoBatch();
        logger.debug("result {}: {}", getSqlId(), execRet);

        return execRet;
    }


    private Object execDmlInBatch() throws SQLException {
        return batch.processBatchUpdate(currRun);
    }

    private void prepareStmt(EStmt stmt) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = prepareSql();

            stmt.setPreparedStatment(ps);
            stmt.setEqlRun(currRun);
            stmt.setLogger(logger);
            stmt.setParams(params);
            stmt.setEqlTran(externalTran != null ? externalTran : internalTran);
        } catch (Exception ex) {
            EqlUtils.closeQuietly(ps);
        }
    }

    private Object execDmlNoBatch() throws SQLException {
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = prepareSql();
            new EqlParamsBinder().bindParams(ps, currRun, logger);

            if (currRun.getSqlType() == EqlRun.EqlType.SELECT) {
                rs = ps.executeQuery();
                if (fetchSize > 0) rs.setFetchSize(fetchSize);

                return rsRetriever.convert(rs, currRun);
            }

            if (EqlUtils.isProcedure(currRun.getSqlType()))
                return new EqlProc(currRun, rsRetriever).dealProcedure(ps);

            return ps.executeUpdate();

        } finally {
            EqlUtils.closeQuietly(rs, ps);
        }
    }

    private PreparedStatement prepareSql() throws SQLException {
        return prepareSql(currRun);
    }

    public PreparedStatement prepareSql(EqlRun eqlRun) throws SQLException {
        logger.debug("prepare sql {}: {} ", getSqlId(), eqlRun.getPrintSql());
        return EqlUtils.isProcedure(eqlRun.getSqlType())
                ? connection.prepareCall(eqlRun.getRunSql()) : connection.prepareStatement(eqlRun.getRunSql());
    }

    public Eql returnType(Class<?> returnType) {
        rsRetriever.setReturnType(returnType);
        return this;
    }

    public Eql returnType(EqlRowMapper eqlRowMapper) {
        rsRetriever.setEqlRowMapper(eqlRowMapper);
        return this;
    }

    protected Eql(Object connectionNameOrConfigable) {
        this.connectionNameOrConfigable = connectionNameOrConfigable;
    }

    public Eql(String connectionName) {
        this.connectionNameOrConfigable = connectionName;
    }

    public Eql(EqlConfigable eqlConfigable) {
        this.connectionNameOrConfigable = eqlConfigable;
    }

    public Eql() {
        this(DEFAULT_CONN_NAME);
    }

    protected void initSqlId(String sqlId, String sqlClassPath) {
        this.sqlClassPath = Strings.isNullOrEmpty(sqlClassPath) ? EqlUtils.getSqlClassPath(4) : sqlClassPath;

        eqlBlock = SqlResourceLoader.load(this.sqlClassPath, sqlId);

        rsRetriever.setEqlBlock(eqlBlock);
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
        return eqlBlock.getSqlId();
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
