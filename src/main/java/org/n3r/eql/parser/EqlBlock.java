package org.n3r.eql.parser;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
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
    @Getter Map<String, String> options;
    @Getter Class<?> returnType;
    @Getter String split;

    @Getter @Setter List<Sql> sqls = Lists.newArrayList();
    @Getter @Setter Collection<String> sqlLines;
    @Getter EqlUniqueSqlId uniqueSqlId;
    private EqlCacheProvider cacheProvider;
    @Getter @Setter String returnTypeName;
    @Getter boolean iterateOption;
    @Getter List<CodeDesc> codeDescs;
    @Getter @Setter boolean override;
    @Getter boolean onErrResume;

    public EqlBlock(String sqlClassPath, String sqlId, String options, int startLineNo) {
        this.uniqueSqlId = new EqlUniqueSqlId(sqlClassPath, sqlId);
        this.options = BlockOptionsParser.parseOptions(options);

        initSomeOptions();
    }

    public EqlBlock(String options) {
        this.uniqueSqlId = new EqlUniqueSqlId("<DirectSql>", "auto");
        this.options = BlockOptionsParser.parseOptions(options);

        initSomeOptions();
    }

    private void initSomeOptions() {
        String onerr = options.get("onerr");
        onErrResume = "resume".equalsIgnoreCase(onerr);
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

        cacheProvider = EqlCacheSettings.getCacheProvider(uniqueSqlId, cacheModel);
    }

    public List<EqlRun> createEqlRuns(
            String tagSqlId, EqlConfigDecorator eqlConfig,
            Map<String, Object> executionContext,
            Object[] params, Object[] dynamics, String[] directSqls) {
        return directSqls.length == 0
                ? createEqlRunsByEqls(tagSqlId, eqlConfig,
                executionContext, params, dynamics)
                : createEqlRunsByDirectSqls(tagSqlId, eqlConfig,
                executionContext, params, dynamics, directSqls);
    }

    public List<EqlRun> createEqlRunsByEqls(
            String tagSqlId,
            EqlConfigDecorator eqlConfig,
            Map<String, Object> executionContext,
            Object[] params, Object[] dynamics) {
        Object paramBean = O.createSingleBean(params);

        List<EqlRun> eqlRuns = Lists.newArrayList();
        EqlRun lastSelectSql = null;
        for (Sql sql : sqls) {
            EqlRun eqlRun = newEqlRun(tagSqlId, eqlConfig,
                    executionContext, params, dynamics, paramBean);

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

    public List<EqlRun> createEqlRunsByDirectSqls(
            String tagSqlId, EqlConfigDecorator eqlConfig,
            Map<String, Object> executionContext,
            Object[] params, Object[] dynamics, String[] sqls) {

        parseDirectSqlBlock(eqlConfig, sqls);

        return createEqlRunsByEqls(tagSqlId, eqlConfig, executionContext, params, dynamics);
    }

    private void parseDirectSqlBlock(EqlConfigDecorator eqlConfig, String[] sqls) {
        val langDriver = eqlConfig.getSqlResourceLoader().getDynamicLanguageDriver();
        val blockParser = new EqlBlockParser(langDriver, false);
        List<String> sqlLines = Lists.newArrayList();
        char sqlSplit = split.charAt(0);
        Splitter sqlSplitter = Splitter.on(sqlSplit).trimResults().omitEmptyStrings();
        Splitter lineSplitter = Splitter.onPattern("[\n\n]").omitEmptyStrings();
        for (String sqlStr : sqls) {
            for (String sql : sqlSplitter.split(sqlStr)) {
                for (String line : lineSplitter.split(sql)) {
                    sqlLines.add(line);
                }
                sqlLines.add(";");
            }
        }

        blockParser.parse(this, sqlLines);
    }

    private void addEqlRun(EqlConfigDecorator eqlConfig, EqlRun eqlRun, String sqlStr) {
        EqlParamsParser.parseParams(eqlRun, sqlStr);
        new DynamicReplacer().replaceDynamics(eqlConfig, eqlRun);
    }

    private EqlRun newEqlRun(
            String tagSqlId, EqlConfigDecorator eqlConfig,
            Map<String, Object> executionContext, Object[] params,
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

    public void tryParseSqls() {
        for (Sql sql : sqls) {
            if (sql instanceof DelaySql) {
                ((DelaySql) sql).parseSql();
            }
        }
    }

    public String getUniqueSqlIdStr() {
        return uniqueSqlId.getSqlClassPath() + ":" + uniqueSqlId.getSqlId();
    }

    public String getSqlId() {
        return uniqueSqlId.getSqlId();
    }

    public Optional<Object> getCachedResult(
            Object[] params, Object[] dynamics, EqlPage page) {
        if (cacheProvider == null) return null;

        EqlCacheKey cacheKey = new EqlCacheKey(uniqueSqlId, params, dynamics, page);
        val cache = cacheProvider.getCache(cacheKey);
        if (cache != null && page != null) {
            val totalRowSqlId = uniqueSqlId.newTotalRowSqlId();
            cacheKey = new EqlCacheKey(totalRowSqlId, params, dynamics, page);
            val totalNumber = cacheProvider.getCache(cacheKey);
            if (totalNumber.isPresent())
                page.setTotalRows((Integer) totalNumber.get());
        }

        return cache;
    }

    public void cacheResult(EqlRun currRun, EqlPage page) {
        if (cacheProvider == null) return;
        if (!currRun.isLastSelectSql()) return;

        EqlCacheKey cacheKey = new EqlCacheKey(uniqueSqlId,
                currRun.getParams(), currRun.getDynamics(), page);
        cacheProvider.setCache(cacheKey, currRun.getResult());

        if (page != null) {
            val totalRowSqlId = uniqueSqlId.newTotalRowSqlId();
            cacheKey = new EqlCacheKey(totalRowSqlId,
                    currRun.getParams(), currRun.getDynamics(), page);
            cacheProvider.setCache(cacheKey, page.getTotalRows());
        }
    }
}
