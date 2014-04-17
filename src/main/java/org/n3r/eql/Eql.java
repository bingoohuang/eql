package org.n3r.eql;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.config.EqlConfigCache;
import org.n3r.eql.config.EqlConfigDecorator;
import org.n3r.eql.config.EqlConfigManager;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.impl.DefaultEqlConfigDecorator;
import org.n3r.eql.impl.EqlBatch;
import org.n3r.eql.impl.EqlProc;
import org.n3r.eql.impl.EqlRsRetriever;
import org.n3r.eql.map.EqlRowMapper;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.param.EqlParamsBinder;
import org.n3r.eql.parser.EqlBlock;
import org.n3r.eql.util.EqlUtils;
import org.n3r.eql.util.HostAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;
import java.util.Map;

public class Eql {
    public static final String DEFAULT_CONN_NAME = "DEFAULT";
    protected static Logger logger = LoggerFactory.getLogger(Eql.class);

    protected EqlConfigDecorator eqlConfig;
    protected EqlBlock eqlBlock;
    protected Object[] params;
    private String sqlClassPath;
    private EqlPage page;
    protected EqlBatch batch;
    protected Object[] dynamics;
    private EqlTran externalTran;
    private EqlTran internalTran;
    private DbDialect dbDialect;
    protected EqlRsRetriever rsRetriever = new EqlRsRetriever();
    private int fetchSize;
    protected List<EqlRun> eqlRuns;
    protected EqlRun currRun;
    protected Map<String, Object> executionContext;
    private String defaultSqlId;
    private boolean cached = true;

    public Eql() {
        init(EqlConfigCache.getEqlConfig(DEFAULT_CONN_NAME), 4);
    }

    public Eql(String connectionName) {
        init(EqlConfigCache.getEqlConfig(connectionName), 4);
    }

    public Eql(EqlConfig eqlConfig) {
        init(eqlConfig, 4);
    }

    public Eql(EqlConfig eqlConfig, int stackDeep) {
        init(eqlConfig, stackDeep);
    }

    private void init(EqlConfig eqlConfig, int stackDeep) {
        this.eqlConfig = eqlConfig instanceof EqlConfigDecorator
                ? (EqlConfigDecorator) eqlConfig
                : new DefaultEqlConfigDecorator(eqlConfig);

        prepareDefaultSqlId(stackDeep);
    }

    private void prepareDefaultSqlId(int stackDeep) {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[stackDeep];
        defaultSqlId = e.getMethodName();
    }

    public Connection getConnection() {
        return newTran(eqlConfig, this).getConn();
    }

    protected Connection getConn() {
        // if (connection != null) return;

        Connection connection = internalTran != null ? internalTran.getConn() : externalTran.getConn();
        dbDialect = DbDialect.parseDbType(connection);
        executionContext.put("_databaseId", dbDialect.getDatabaseId());

        return connection;
    }

    public Eql addContext(String key, Object value) {
        executionContext.put(key, value);

        return this;
    }

    public List<EqlRun> evaluate(String... directSqls) {
        checkPreconditions(directSqls);

        newExecutionContext();

        if (directSqls.length > 0) eqlBlock = new EqlBlock();

        List<EqlRun> runs = eqlBlock.createEqlRuns(eqlConfig,
                executionContext, params, dynamics, directSqls);

        if (logger.isDebugEnabled()) {
            for (EqlRun run : runs) {
                logger.debug(run.getPrintSql());
            }
        }

        return runs;
    }

    @SuppressWarnings("unchecked")
    public <T> T execute(String... directSqls) {
        checkPreconditions(directSqls);

        boolean cacheUsable = directSqls.length == 0 && cached;
        Optional<Object> cachedResult = cacheUsable
                ? eqlBlock.getCachedResult(params, dynamics) : null;

        if (cachedResult != null) return (T) cachedResult.orNull();

        newExecutionContext();
        Object ret = null;
        Connection conn = null;
        try {
            if (batch == null) tranStart();
            conn = getConn();

            if (directSqls.length > 0) eqlBlock = new EqlBlock();

            eqlRuns = eqlBlock.createEqlRuns(eqlConfig, executionContext, params, dynamics, directSqls);
            for (EqlRun eqlRun : eqlRuns) {
                currRun = eqlRun;
                new EqlParamsBinder().preparBindParams(currRun);
                checkBatchCmdsSupporting(currRun);

                currRun.setConnection(conn);
                ret = runEql();
                currRun.setConnection(null);
                updateLastResultToExecutionContext(ret);
                currRun.setResult(ret);

                if (cacheUsable) eqlBlock.cacheResult(currRun);
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

    private void checkBatchCmdsSupporting(EqlRun currRun) {
        if (batch == null) return;

        switch (currRun.getSqlType()) {
            case INSERT:
            case UPDATE:
            case MERGE:
            case DELETE:
                // OK!
                break;
            default:
                throw new EqlExecuteException(currRun.getPrintSql() + " is not supported in batch mode");
        }
    }

    protected void updateLastResultToExecutionContext(Object ret) {
        Object lastResult = ret;
        if (ret instanceof List) {
            List<Object> list = (List<Object>) ret;
            if (list.size() == 0) lastResult = null;
            else if (list.size() == 1) lastResult = list.get(0);
        }

        executionContext.put("_lastResult", lastResult);
    }

    protected void newExecutionContext() {
        executionContext = Maps.newHashMap();
        executionContext.put("_time", new Timestamp(System.currentTimeMillis()));
        executionContext.put("_date", new java.util.Date());
        executionContext.put("_host", HostAddress.getHost());
        executionContext.put("_ip", HostAddress.getIp());
        executionContext.put("_results", Lists.newArrayList());
        executionContext.put("_lastResult", "");
        executionContext.put("_params", params);
        if (params != null) {
            executionContext.put("_paramsCount", params.length);
            for (int i = 0; i < params.length; ++i)
                executionContext.put("_" + (i + 1), params[i]);
        }


        executionContext.put("_dynamics", dynamics);
        if (dynamics != null) executionContext.put("_dynamicsCount", dynamics.length);
    }

    public List<EqlRun> getEqlRuns() {
        return eqlRuns;
    }

    protected void resetState() {
        rsRetriever.resetMaxRows();
    }

    public void close() {
        if (batch == null) tranClose();
    }


    public ESelectStmt selectStmt() {
        newExecutionContext();
        tranStart();
        Connection conn = getConn();

        List<EqlRun> sqlSubs = eqlBlock.createEqlRunsByEqls(eqlConfig, executionContext, params, dynamics);
        if (sqlSubs.size() != 1)
            throw new EqlExecuteException("only one select sql supported");

        currRun = sqlSubs.get(0);
        if (currRun.getSqlType() != EqlRun.EqlType.SELECT)
            throw new EqlExecuteException("only one select sql supported");

        currRun.setConnection(conn);
        ESelectStmt selectStmt = new ESelectStmt();
        prepareStmt(selectStmt);

        selectStmt.setRsRetriever(rsRetriever);
        selectStmt.setFetchSize(fetchSize);

        return selectStmt;
    }

    public EUpdateStmt updateStmt() {
        newExecutionContext();
        tranStart();
        Connection conn = getConn();

        List<EqlRun> sqlSubs = eqlBlock.createEqlRunsByEqls(eqlConfig, executionContext, params, dynamics);
        if (sqlSubs.size() != 1)
            throw new EqlExecuteException("only one update sql supported in this method");

        currRun = sqlSubs.get(0);
        if (!EqlUtils.isUpdateStmt(currRun))
            throw new EqlExecuteException("only one update/merge/delete/insert sql supported in this method");

        currRun.setConnection(conn);
        EUpdateStmt updateStmt = new EUpdateStmt();
        prepareStmt(updateStmt);

        return updateStmt;
    }

    protected void checkPreconditions(String... directSqls) {
        if (eqlBlock != null || directSqls.length > 0) return;

        if (EqlUtils.isBlank(defaultSqlId)) throw new EqlExecuteException("No sqlid defined!");

        initSqlId(defaultSqlId, 5);
    }

    protected Object runEql() throws SQLException {
        try {
            return EqlUtils.isDdl(currRun) ? execDdl() : pageExecute();
        } catch (Exception ex) {
            if (!currRun.getEqlBlock().isOnerrResume()) throw Throwables.propagate(ex);
            else logger.warn("execute sql {} error", currRun.getPrintSql(), ex);
        }

        return 0;
    }

    private boolean execDdl() {
        Statement stmt = null;
        logger.debug("ddl sql {}: {}", getSqlId(), currRun.getPrintSql());
        try {
            stmt = currRun.getConnection().createStatement();
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
        currRun = dbDialect.createPageSql(currRun, page);

        new EqlParamsBinder().preparBindParams(currRun);

        Object o = execDml();
        currRun = temp;

        return o;
    }

    private int executeTotalRowsSql() throws SQLException {
        EqlRun temp = currRun;
        currRun = dbDialect.createTotalSql(currRun);
        Object totalRows = execDml();
        currRun = temp;

        if (totalRows instanceof Number) return ((Number) totalRows).intValue();

        throw new EqlExecuteException("returned total rows object " + totalRows + " is not a number");
    }

    private Object execDml() throws SQLException {
        Object execRet = batch != null ? execDmlInBatch() : execDmlNoBatch();
        logger.debug("result {}: {}", getSqlId(), execRet);

        return execRet;
    }


    private Object execDmlInBatch() throws SQLException {
        return batch.addBatch(currRun);
    }

    private void prepareStmt(EStmt stmt) {
        PreparedStatement ps = null;
        try {
            ps = prepareSql();

            stmt.setPreparedStatment(ps);
            stmt.setEqlRun(currRun);
            stmt.setLogger(logger);
            stmt.params(params);
            stmt.setEqlTran(externalTran != null ? externalTran : internalTran);
        } catch (Exception ex) {
            EqlUtils.closeQuietly(ps);
            throw new EqlExecuteException(ex);
        }
    }

    private Object execDmlNoBatch() throws SQLException {
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = prepareSql();
            currRun.bindParams(ps);

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
                ? eqlRun.getConnection().prepareCall(eqlRun.getRunSql())
                : eqlRun.getConnection().prepareStatement(eqlRun.getRunSql());
    }

    public Eql returnType(Class<?> returnType) {
        rsRetriever.setReturnType(returnType);
        return this;
    }

    public Eql returnType(EqlRowMapper eqlRowMapper) {
        rsRetriever.setEqlRowMapper(eqlRowMapper);
        return this;
    }


    protected void initSqlId(String sqlId) {
        initSqlId(sqlId, 5);
    }

    protected void initSqlId(String sqlId, int level) {
        this.sqlClassPath = Strings.isNullOrEmpty(sqlClassPath)
                ? EqlUtils.getSqlClassPath(level) : sqlClassPath;

        eqlBlock = eqlConfig.getSqlResourceLoader()
                .loadEqlBlock(this.sqlClassPath, sqlId);

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
        initSqlId(sqlId);
        return this;
    }

    public Eql merge(String sqlId) {
        initSqlId(sqlId);
        return this;
    }

    public Eql update(String sqlId) {
        initSqlId(sqlId);
        return this;
    }

    public Eql insert(String sqlId) {
        initSqlId(sqlId);
        return this;
    }

    public Eql delete(String sqlId) {
        initSqlId(sqlId);
        return this;
    }

    public Eql select(String sqlId) {
        initSqlId(sqlId);
        return this;
    }

    public Eql selectFirst(String sqlId) {
        initSqlId(sqlId);
        limit(1);
        return this;
    }

    public Eql procedure(String sqlId) {
        initSqlId(sqlId);
        return this;
    }

    public String getSqlId() {
        return eqlBlock.getSqlId();
    }

    protected void tranStart() {
        if (externalTran != null) return;
        if (internalTran != null) return;

        internalTran = newTran(eqlConfig, this);
        internalTran.start();
    }

    protected void tranCommit() {
        if (externalTran != null) return;

        internalTran.commit();
    }

    private void tranClose() {
        if (internalTran != null) EqlUtils.closeQuietly(internalTran);

        internalTran = null;
    }

    public EqlTran newTran() {
        EqlTran tran = newTran(eqlConfig, this);
        // connection = tran.getConn();
        useTran(tran);

        return tran;
    }

    public Eql useTran(EqlTran tran) {
        externalTran = tran;
        return this;
    }

    public static EqlTran newTran(EqlConfig eqlConfig, Eql eql) {
        return EqlConfigManager.getConfig(eqlConfig).createTran(eql);
    }

    public EqlConfig getEqlConfig() {
        return eqlConfig;
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

    public Eql startBatch() {
        return startBatch(0);
    }

    public Eql startBatch(int maxBatches) {
        batch = new EqlBatch(this);
        batch.startBatch(maxBatches);

        tranStart();
        return this;
    }

    public int executeBatch() {
        int totalRows = 0;
        try {
            totalRows = batch.executeBatch();
            tranCommit();
        } catch (SQLException e) {
            throw new EqlExecuteException("executeBatch failed:" + e.getMessage());
        } finally {
            tranClose();
            batch = null;
        }

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

    public Eql cached(boolean cached) {
        this.cached = cached;
        return this;
    }

    public void resetTran() {
        externalTran = null;
        internalTran = null;
    }

}
