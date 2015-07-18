package org.n3r.eql.parser;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.n3r.eql.EqlPage;
import org.n3r.eql.cache.EqlCacheKey;
import org.n3r.eql.cache.EqlCacheProvider;
import org.n3r.eql.cache.EqlCacheSettings;
import org.n3r.eql.codedesc.CodeDesc;
import org.n3r.eql.codedesc.CodeDescs;
import org.n3r.eql.config.EqlConfigDecorator;
import org.n3r.eql.impl.EqlUniqueSqlId;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.map.EqlType;
import org.n3r.eql.param.EqlParamsParser;
import org.n3r.eql.util.C;
import org.n3r.eql.util.EqlUtils;
import org.n3r.eql.util.O;
import org.n3r.eql.util.S;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EqlBlock {
    private int startLineNo;
    private Map<String, String> options = Maps.newHashMap();
    private Class<?> returnType;
    private String onerr;
    private String split;

    private List<Sql> sqls = Lists.newArrayList();
    private Collection<String> sqlLines;
    private EqlUniqueSqlId uniquEQLId;
    private EqlCacheProvider cacheProvider;
    private String returnTypeName;
    private boolean iterateOption;
    private List<CodeDesc> codeDescs;
    private boolean override;

    public EqlBlock(String sqlClassPath, String sqlId, String options, int startLineNo) {
        this.uniquEQLId = new EqlUniqueSqlId(sqlClassPath, sqlId);
        this.startLineNo = startLineNo;
        this.options = BlockOptionsParser.parseOptions(options);

        initSomeOptions();
    }

    public EqlBlock() {
        this.uniquEQLId = new EqlUniqueSqlId("<DirectSql>", "auto");
    }

    private void initSomeOptions() {
        onerr = options.get("onerr");
        returnTypeName = options.get("returnType");
        iterateOption = options.containsKey("iterate");
        codeDescs = CodeDescs.parseOption(this, options.get("desc"));
        returnType = C.tryLoadClass(returnTypeName);
        override = options.containsKey("override");

        split = options.get("split");
        if (Strings.isNullOrEmpty(split)) split = ";";

        initEqlCache(options.containsKey("cache"), options.get("cacheModel"));
    }

    private void initEqlCache(boolean useCache, String cacheModel) {
        if (Strings.isNullOrEmpty(cacheModel) && !useCache) return;

        cacheProvider = EqlCacheSettings.getCacheProvider(uniquEQLId, cacheModel);
    }

    public List<Sql> getSqls() {
        return sqls;
    }

    public List<EqlRun> createEqlRuns(String tagSqlId, EqlConfigDecorator eqlConfig, Map<String, Object> executionContext,
                                      Object[] params, Object[] dynamics, String[] directSqls) {
        return directSqls.length == 0
                ? createEqlRunsByEqls(tagSqlId, eqlConfig, executionContext, params, dynamics)
                : createEqlRunsByDirectSqls(tagSqlId, eqlConfig, executionContext, params, dynamics, directSqls);
    }

    public List<EqlRun> createEqlRunsByEqls(String tagSqlId, EqlConfigDecorator eqlConfig, Map<String, Object> executionContext,
                                            Object[] params, Object[] dynamics) {
        Object paramBean = O.createSingleBean(params);

        List<EqlRun> eqlRuns = Lists.newArrayList();
        EqlRun lastSelectSql = null;
        for (Sql sql : sqls) {
            EqlRun eqlRun = newEqlRun(tagSqlId, eqlConfig, executionContext, params, dynamics, paramBean);

            String sqlStr = sql.evalSql(eqlRun);
            sqlStr = EqlUtils.trimLastUnusedPart(sqlStr);

            if (S.isBlank(sqlStr)) continue;

            eqlRuns.add(eqlRun);
            addEqlRun(eqlConfig, eqlRun, sqlStr);
            if (eqlRun.getSqlType() == EqlType.SELECT) lastSelectSql = eqlRun;
        }

        if (lastSelectSql != null) lastSelectSql.setLastSelectSql(true);

        return eqlRuns;
    }


    private void addEqlRun(EqlConfigDecorator eqlConfig, EqlRun eqlRun, String sqlStr) {
        EqlParamsParser.parseParams(eqlRun, sqlStr);
        new DynamicReplacer().replaceDynamics(eqlConfig, eqlRun);
    }

    public List<EqlRun> createEqlRunsByDirectSqls(String tagSqlId, EqlConfigDecorator eqlConfig, Map<String, Object> executionContext,
                                                  Object[] params, Object[] dynamics, String[] sqls) {
        Object paramBean = O.createSingleBean(params);

        List<EqlRun> eqlRuns = Lists.newArrayList();
        EqlRun lastSelectSql = null;
        int sqlNo = 0;
        for (String sql : sqls) {
            String seqTagSqlId =  sqls.length == 1 ? tagSqlId : (tagSqlId + "." + (++sqlNo));
            EqlRun eqlRun = newEqlRun(seqTagSqlId, eqlConfig, executionContext, params, dynamics, paramBean);

            eqlRuns.add(eqlRun);
            addEqlRun(eqlConfig, eqlRun, sql);

            if (eqlRun.getSqlType() == EqlType.SELECT) lastSelectSql = eqlRun;
        }

        if (lastSelectSql != null) lastSelectSql.setLastSelectSql(true);

        return eqlRuns;
    }


    private EqlRun newEqlRun(String tagSqlId, EqlConfigDecorator eqlConfig, Map<String, Object> executionContext, Object[] params,
                             Object[] dynamics, Object paramBean) {
        EqlRun eqlRun = new EqlRun();

        eqlRun.setEqlConfig(eqlConfig);
        eqlRun.setTagSqlId(tagSqlId);
        eqlRun.setExecutionContext(executionContext);
        eqlRun.setParams(params);
        eqlRun.setDynamics(dynamics);
        eqlRun.setParamBean(paramBean);
        eqlRun.setEqlBlock(this);
        return eqlRun;
    }

    public boolean isOnerrResume() {
        return "resume".equalsIgnoreCase(onerr);
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setSqls(List<Sql> sqls) {
        this.sqls = sqls;
    }

    public Collection<? extends String> getSqlLines() {
        return sqlLines;
    }

    public void setSqlLines(List<String> sqlLines) {
        this.sqlLines = sqlLines;
    }

    public String getSplit() {
        return split;
    }

    public void tryParseSqls() {
        for (Sql sql : sqls) {
            if (sql instanceof DelaySql) {
                ((DelaySql) sql).parseSql();
            }
        }
    }

    public EqlUniqueSqlId getUniqueSqlId() {
        return uniquEQLId;
    }

    public String getUniqueSqlIdStr() {
        return uniquEQLId.getSqlClassPath() + ":" + uniquEQLId.getSqlId();
    }

    public String getSqlId() {
        return uniquEQLId.getSqlId();
    }

    public Optional<Object> getCachedResult(Object[] params, Object[] dynamics, EqlPage page) {
        if (cacheProvider == null) return null;

        EqlCacheKey cacheKey = new EqlCacheKey(uniquEQLId, params, dynamics, page);
        Optional<Object> cache = cacheProvider.getCache(cacheKey);
        if (cache != null && page != null) {
            EqlUniqueSqlId totalRowSqlId = uniquEQLId.newTotalRowSqlId();
            cacheKey = new EqlCacheKey(totalRowSqlId, params, dynamics, page);
            Optional<Object> totalNumber = cacheProvider.getCache(cacheKey);
            if (totalNumber.isPresent()) page.setTotalRows((Integer) totalNumber.get());
        }

        return cache;
    }

    public void cacheResult(EqlRun currRun, EqlPage page) {
        if (cacheProvider == null) return;
        if (!currRun.isLastSelectSql()) return;

        EqlCacheKey cacheKey = new EqlCacheKey(uniquEQLId, currRun.getParams(), currRun.getDynamics(), page);
        cacheProvider.setCache(cacheKey, currRun.getResult());

        if (page != null) {
            EqlUniqueSqlId totalRowSqlId = uniquEQLId.newTotalRowSqlId();
            cacheKey = new EqlCacheKey(totalRowSqlId, currRun.getParams(), currRun.getDynamics(), page);
            cacheProvider.setCache(cacheKey, page.getTotalRows());
        }
    }

    public String getReturnTypeName() {
        return returnTypeName;
    }

    public void setReturnTypeName(String returnTypeName) {
        this.returnTypeName = returnTypeName;
    }

    public boolean hasIterateOption() {
        return iterateOption;
    }

    public List<CodeDesc> getCodeDescs() {
        return codeDescs;
    }

    public boolean isOverride() {
        return override;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }
}
