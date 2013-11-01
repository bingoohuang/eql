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
    private final int startLineNo;
    private final String sqlClassPath;
    private String sqlId;
    private Map<String, String> options = Maps.newHashMap();
    private Class<?> returnType;
    private String onerr;
    private String split;

    private List<Eql> eqls = Lists.newArrayList();
    private Collection<String> sqlLines;


    public EqlBlock(String sqlClassPath, String sqlId, String options, int startLineNo) {
        this.sqlClassPath = sqlClassPath;
        this.sqlId = sqlId;
        this.startLineNo = startLineNo;
        this.options = BlockOptionsParser.parseOptions(options);

        initSomeOptions();
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

    public List<Eql> getEqls() {
        return eqls;
    }


    public List<EqlRun> createSqlSubs(Object[] params, Object[] dynamics, String... directSqls) {
        return directSqls.length == 0
                ? createSqlSubs(params, dynamics)
                : createSqlSubsByDirectSqls(dynamics, directSqls);
    }

    public List<EqlRun> createSqlSubs(Object[] params, Object[] dynamics) {
        Object bean = EqlUtils.compositeParams(params);

        List<EqlRun> eqlRuns = Lists.newArrayList();
        EqlRun lastSelectSql = null;
        for (Eql eql : eqls) {
            String sqlStr = eql.evalSql(bean);
            sqlStr = EqlUtils.autoTrimLastUnusedPart(sqlStr);

            if (Strings.isNullOrEmpty(sqlStr)) continue;

            EqlRun eqlRun = new EqlParamsParser().parseParams(sqlStr, this);
            eqlRuns.add(eqlRun);

            createRunSql(eqlRun, dynamics);
            if (eqlRun.getSqlType() == EqlRun.EqlType.SELECT) lastSelectSql = eqlRun;
        }

        if (lastSelectSql != null) lastSelectSql.setLastSelectSql(true);

        return eqlRuns;
    }

    private void createRunSql(EqlRun eqlRun, Object[] dynamics) {
        new DynamicReplacer().replaceDynamics(eqlRun, dynamics);

        eqlRun.createPrintSql();
    }

    public List<EqlRun> createSqlSubsByDirectSqls(Object[] dynamics, String[] sqls) {
        List<EqlRun> eqlRuns = Lists.newArrayList();
        EqlRun lastSelectSql = null;
        for (String sql : sqls) {
            EqlRun eqlRun = new EqlParamsParser().parseParams(sql, null);
            eqlRuns.add(eqlRun);
            createRunSql(eqlRun, dynamics);

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

    public void setEqls(List<Eql> eqls) {
        this.eqls = eqls;
    }

    public Collection<? extends String> getSqlLines() {
        return sqlLines;
    }

    public void setSqlLines(List<String> sqlLines) {
        this.sqlLines = sqlLines;
    }
}
