package org.n3r.eql;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import org.n3r.eql.codedesc.CodeDescs;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.config.EqlConfigCache;
import org.n3r.eql.config.EqlConfigDecorator;
import org.n3r.eql.config.EqlConfigManager;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.impl.*;
import org.n3r.eql.map.EqlRowMapper;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.map.EqlType;
import org.n3r.eql.param.EqlParamsBinder;
import org.n3r.eql.parser.EqlBlock;
import org.n3r.eql.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Eql {
    public static final String DEFAULT_CONN_NAME = "DEFAULT";
    public static final int STACKTRACE_DEEP_FOUR = 4;
    public static final int STACKTRACE_DEEP_FIVE = 5;

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
    protected Map<String, Object> execContext;
    private String defaultSqlId;
    private boolean cached = true;

    public Eql() {
        this(Eql.STACKTRACE_DEEP_FIVE);
    }

    public Eql(int stackDeep) {
        EqlConfig localConfig = Eqll.eqlConfigLocal.get();
        if (localConfig == null) {
            init(EqlConfigCache.getEqlConfig(DEFAULT_CONN_NAME), stackDeep);
        } else {
            init(localConfig, stackDeep);
        }
    }

    public Eql(String connectionName) {
        init(EqlConfigCache.getEqlConfig(connectionName), STACKTRACE_DEEP_FOUR);
    }

    public Eql(EqlConfig eqlConfig) {
        init(eqlConfig, STACKTRACE_DEEP_FOUR);
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
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stackTrace[stackDeep];
        defaultSqlId = e.getMethodName();
    }

    public Connection getConnection() {
        return newTran(eqlConfig).getConn(eqlConfig, currRun);
    }

    private void createDbDialect() {
        EqlTran eqlTran = internalTran != null ? internalTran : externalTran;
        dbDialect = DbDialect.parseDbType(eqlTran.getDriverName(), eqlTran.getJdbcUrl());
        execContext.put("_databaseId", dbDialect.getDatabaseId());
    }

    protected void createConn() {
        (internalTran != null ? internalTran : externalTran).getConn(eqlConfig, currRun);
    }

    public Eql addContext(String key, Object value) {
        execContext.put(key, value);

        return this;
    }

    public List<EqlRun> evaluate(String... directSqls) {
        checkPreconditions(directSqls);

        execContext = EqlUtils.newExecContext(params, dynamics);

        if (directSqls.length > 0) eqlBlock = new EqlBlock();

        List<EqlRun> runs = eqlBlock.createEqlRuns(eqlConfig,
                execContext, params, dynamics, directSqls);

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

        Object o = tryGetCache(directSqls);
        if (o != null) return (T) o;

        execContext = EqlUtils.newExecContext(params, dynamics);
        Object ret = null;
        try {
            tranStart();
            createDbDialect();

            if (directSqls.length > 0) eqlBlock = new EqlBlock();

            eqlRuns = eqlBlock.createEqlRuns(eqlConfig, execContext, params, dynamics, directSqls);
            IterateOptions.checkIterateOption(eqlBlock, eqlRuns, params);

            for (EqlRun eqlRun : eqlRuns) {
                currRun = eqlRun;
                if (S.isBlank(currRun.getRunSql())) continue;

                checkBatchCmdsSupporting(eqlRun);
                new EqlParamsBinder().prepareBindParams(eqlBlock.hasIterateOption(), currRun);
                createConn();

                ret = runEql();
                currRun.setConnection(null);
                updateLastResultToExecutionContext(ret);
                currRun.setResult(ret);

                trySetCache(directSqls);
            }

            if (batch == null) tranCommit();
        } catch (Throwable e) {
            logger.error("exception", e);
            throw new EqlExecuteException("exec sql failed["
                    + currRun.getPrintSql() + "]" + e.getMessage());
        } finally {
            resetState();
            close();
        }

        return (T) ret;
    }

    private void trySetCache(String[] directSqls) {
        if (!isCacheUsable(directSqls)) return;

        eqlBlock.cacheResult(currRun, page);
    }

    private Object tryGetCache(String[] directSqls) {
        if (!isCacheUsable(directSqls)) return null;

        Optional<Object> cachedResult = eqlBlock.getCachedResult(params, dynamics, page);
        if (cachedResult == null) return null;

        return cachedResult.orNull();
    }

    private boolean isCacheUsable(String[] directSqls) {
        return directSqls.length == 0 && cached;
    }

    private void checkBatchCmdsSupporting(EqlRun currRun) {
        if (batch == null) return;

        switch (currRun.getSqlType()) {
            case INSERT:
            case UPDATE:
            case MERGE:
            case DELETE:
            case REPLACE:
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

        execContext.put("_lastResult", lastResult);
    }

    public List<EqlRun> getEqlRuns() {
        return eqlRuns;
    }

    protected void resetState() {
        rsRetriever.resetMaxRows();
    }

    public void close() {
        tranClose();
    }

    public ESelectStmt selectStmt() {
        execContext = EqlUtils.newExecContext(params, dynamics);
        tranStart();

        List<EqlRun> sqlSubs = eqlBlock.createEqlRunsByEqls(eqlConfig, execContext, params, dynamics);
        if (sqlSubs.size() != 1)
            throw new EqlExecuteException("only one select sql supported");

        currRun = sqlSubs.get(0);
        if (currRun.getSqlType() != EqlType.SELECT)
            throw new EqlExecuteException("only one select sql supported");

        ESelectStmt selectStmt = new ESelectStmt();
        prepareStmt(selectStmt);

        selectStmt.setRsRetriever(rsRetriever);
        selectStmt.setFetchSize(fetchSize);

        return selectStmt;
    }

    public EUpdateStmt updateStmt() {
        execContext = EqlUtils.newExecContext(params, dynamics);
        tranStart();
        createConn();

        List<EqlRun> sqlSubs = eqlBlock.createEqlRunsByEqls(eqlConfig, execContext, params, dynamics);
        if (sqlSubs.size() != 1)
            throw new EqlExecuteException("only one update sql supported in this method");

        currRun = sqlSubs.get(0);
        if (!currRun.getSqlType().isUpdateStmt())
            throw new EqlExecuteException("only one update/merge/delete/insert sql supported in this method");

        EUpdateStmt updateStmt = new EUpdateStmt();
        prepareStmt(updateStmt);

        return updateStmt;
    }

    protected void checkPreconditions(String... directSqls) {
        if (eqlBlock != null || directSqls.length > 0) return;

        if (S.isBlank(defaultSqlId)) throw new EqlExecuteException("No sqlid defined!");

        initSqlId(defaultSqlId, STACKTRACE_DEEP_FIVE);
    }

    protected Object runEql() throws SQLException {
        try {
            return currRun.getSqlType().isDdl() ? execDdl() : pageExecute();
        } catch (Exception ex) {
            if (!currRun.getEqlBlock().isOnerrResume()) throw Throwables.propagate(ex);
            else logger.warn("execute sql {} error {}", currRun.getPrintSql(), ex.getMessage());
        }

        return 0;
    }

    private boolean execDdl() {
        Statement stmt = null;
        logger.debug("ddl sql for {}: {}", getSqlId(), currRun.getPrintSql());
        try {
            stmt = currRun.getConnection().createStatement();
            EqlUtils.setQueryTimeout(eqlConfig, stmt);
            return stmt.execute(currRun.getRunSql());
        } catch (SQLException ex) {
            throw new EqlExecuteException(ex);
        } finally {
            Closes.closeQuietly(stmt);
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

        new EqlParamsBinder().prepareBindParams(eqlBlock.hasIterateOption(), currRun);

        Object o = execDml();
        currRun = temp;

        return o;
    }

    private int executeTotalRowsSql() throws SQLException {
        EqlRun temp = currRun;

        String returnTypeName = currRun.getEqlBlock().getReturnTypeName();

        currRun = dbDialect.createTotalSql(currRun);
        currRun.getEqlBlock().setReturnTypeName("int");
        Object totalRows = execDml();

        currRun = temp;
        currRun.getEqlBlock().setReturnTypeName(returnTypeName);

        if (totalRows instanceof Number) return ((Number) totalRows).intValue();

        throw new EqlExecuteException("returned total rows object " + totalRows + " is not a number");
    }

    private Object execDml() throws SQLException {
        Object execRet = batch != null ? execDmlInBatch() : execDmlNoBatch();

        Logs.logResult(execRet, getSqlId());

        return execRet;
    }

    private Object execDmlInBatch() throws SQLException {
        return batch.addBatch(eqlConfig, currRun, getSqlId());
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
            Closes.closeQuietly(ps);
            throw new EqlExecuteException(ex);
        }
    }

    private Object execDmlNoBatch() throws SQLException {
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = prepareSql();

            if (eqlBlock.hasIterateOption()) {
                int rowCount = 0;

                if (params[0] instanceof Iterable) {
                    Iterator<Object> iterator = ((Iterable<Object>) params[0]).iterator();
                    for (int i = 0; iterator.hasNext(); ++i, iterator.next()) {
                        currRun.bindBatchParams(ps, i);
                        rowCount += ps.executeUpdate();
                    }
                } else if (params[0] != null && params[0].getClass().isArray()){
                    Object[] arr = (Object[]) params[0];
                    for (int i = 0, ii = arr.length; i < ii; ++i) {
                        currRun.bindBatchParams(ps, i);
                        rowCount += ps.executeUpdate();
                    }
                }
                return rowCount;
            } else {
                currRun.bindParams(ps);

                if (currRun.getSqlType() == EqlType.SELECT) {
                    rs = ps.executeQuery();
                    if (fetchSize > 0) rs.setFetchSize(fetchSize);

                    ResultSet wrapRs = CodeDescs.codeDescWrap(currRun, eqlBlock, eqlConfig, sqlClassPath, rs);
                    Object convertedValue = rsRetriever.convert(wrapRs, currRun);
                    return convertedValue;
                }

                if (currRun.getSqlType().isProcedure())
                    return new EqlProc(currRun, rsRetriever).dealProcedure(ps);

                return ps.executeUpdate();
            }

        } finally {
            Closes.closeQuietly(rs, ps);
        }
    }

    private PreparedStatement prepareSql() throws SQLException {
        createConn();
        return EqlUtils.preparEQL(eqlConfig, currRun, getSqlId());
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
        initSqlId(sqlId, STACKTRACE_DEEP_FIVE);
    }

    protected void initSqlId(String sqlId, int level) {
        this.sqlClassPath = Strings.isNullOrEmpty(sqlClassPath)
                ? C.getSqlClassPath(level) : sqlClassPath;

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

    public Eql replace(String sqlId) {
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
        if (batch != null) return;
        if (externalTran != null) return;
        if (internalTran != null) return;

        internalTran = newTran(eqlConfig);
        internalTran.start();
    }

    protected void tranCommit() {
        if (batch != null) return;
        if (externalTran != null) return;

        internalTran.commit();
    }

    private void tranClose() {
        if (internalTran != null) Closes.closeQuietly(internalTran);

        internalTran = null;
    }

    public EqlTran newTran() {
        EqlTran tran = newTran(eqlConfig);
        useTran(tran);

        return tran;
    }

    public Eql useTran(EqlTran tran) {
        externalTran = tran;
        return this;
    }

    public Eql useBatch(EqlBatch eqlBatch) {
        this.batch = eqlBatch;
        return this;
    }

    public static EqlTran newTran(EqlConfigDecorator eqlConfig) {
        return EqlConfigManager.getConfig(eqlConfig).createTran();
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

    public EqlRun getEqlRun() {
        return eqlRuns.size() > 0 ? eqlRuns.get(eqlRuns.size() - 1) : null;
    }
}
