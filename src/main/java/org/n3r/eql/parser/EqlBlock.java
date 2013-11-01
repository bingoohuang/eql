package org.n3r.eql.parser;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.param.EqlParamsParser;
import org.n3r.eql.util.EqlUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EqlBlock {
    private int startLineNo;
    private String sqlClassPath;
    private String sqlId;
    private Map<String, String> options = Maps.newHashMap();
    private Class<?> returnType;
    private String onerr;
    private String split;

    private List<Sql> sqls = Lists.newArrayList();
    private Collection<String> sqlLines;


    public EqlBlock(String sqlClassPath, String sqlId, String options, int startLineNo) {
        this.sqlClassPath = sqlClassPath;
        this.sqlId = sqlId;
        this.startLineNo = startLineNo;
        this.options = BlockOptionsParser.parseOptions(options);

        initSomeOptions();
    }

    public EqlBlock() {

    }

    private void initSomeOptions() {
        onerr = options.get("onerr");
        returnType = EqlUtils.tryLoadClass(options.get("returnType"));

        split = options.get("split");
        if (Strings.isNullOrEmpty(split)) split = ";";
    }

    public String getSqlId() {
        return sqlId;
    }

    public List<Sql> getSqls() {
        return sqls;
    }

    public List<EqlRun> createEqlRuns(Map<String, Object> executionContext,
                                      Object[] params, Object[] dynamics, String[] directSqls) {
        return directSqls.length == 0
                ? createEqlRunsByEqls(executionContext, params, dynamics)
                : createSqlSubsByDirectSqls(executionContext, params, dynamics, directSqls);
    }

    public List<EqlRun> createEqlRunsByEqls(Map<String, Object> executionContext,
                                            Object[] params, Object[] dynamics) {
        Object paramBean = EqlUtils.createSingleBean(params);

        List<EqlRun> eqlRuns = Lists.newArrayList();
        EqlRun lastSelectSql = null;
        for (Sql sql : sqls) {
            String sqlStr = sql.evalSql(paramBean, executionContext);
            sqlStr = EqlUtils.autoTrimLastUnusedPart(sqlStr);

            if (Strings.isNullOrEmpty(sqlStr)) continue;

            EqlRun eqlRun = addEqlRun(executionContext, params, dynamics,
                    paramBean, eqlRuns, sqlStr, this);

            if (eqlRun.getSqlType() == EqlRun.EqlType.SELECT) lastSelectSql = eqlRun;
        }

        if (lastSelectSql != null) lastSelectSql.setLastSelectSql(true);

        return eqlRuns;
    }

    private EqlRun addEqlRun(Map<String, Object> executionContext,
                             Object[] params, Object[] dynamics, Object paramBean,
                             List<EqlRun> eqlRuns, String sqlStr, EqlBlock eqlBlock) {
        EqlRun eqlRun = new EqlParamsParser().parseParams(sqlStr, eqlBlock);
        eqlRuns.add(eqlRun);

        eqlRun.setExecutionContext(executionContext);
        eqlRun.setParams(params);
        eqlRun.setDynamics(dynamics);
        eqlRun.setParamBean(paramBean);

        createRunSql(eqlRun, dynamics);
        return eqlRun;
    }

    private void createRunSql(EqlRun eqlRun, Object[] dynamics) {
        new DynamicReplacer().replaceDynamics(eqlRun, dynamics);

        eqlRun.createPrintSql();
    }

    public List<EqlRun> createSqlSubsByDirectSqls(Map<String, Object> executionContext, Object[] params, Object[] dynamics, String[] sqls) {
        Object paramBean = EqlUtils.createSingleBean(params);

        List<EqlRun> eqlRuns = Lists.newArrayList();
        EqlRun lastSelectSql = null;
        for (String sql : sqls) {
            EqlRun eqlRun = addEqlRun(executionContext, params, dynamics,
                    paramBean, eqlRuns, sql, null);

            if (eqlRun.getSqlType() == EqlRun.EqlType.SELECT) lastSelectSql = eqlRun;
        }

        if (lastSelectSql != null) lastSelectSql.setLastSelectSql(true);

        return eqlRuns;
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
}
