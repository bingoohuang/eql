package org.n3r.eql.map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.n3r.eql.config.EqlConfigDecorator;
import org.n3r.eql.param.EqlParamPlaceholder;
import org.n3r.eql.param.EqlParamsParser;
import org.n3r.eql.param.PlaceholderType;
import org.n3r.eql.parser.EqlBlock;
import org.n3r.eql.util.*;
import org.slf4j.Logger;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class EqlRun implements Cloneable {
    public List<Pair<Integer, Object>> realParams = Lists.<Pair<Integer, Object>>newArrayList();

    @Setter List<Object> boundParams;
    @Getter @Setter Connection connection;
    @Getter String evalSql;
    @Setter String evalSqlTemplate;
    @Setter @Getter EqlDynamic evalEqlDynamic;
    @Setter @Getter boolean iterateOption;
    @Setter @Getter String tagSqlId;
    @Setter @Getter boolean forEvaluate;
    @Getter Map<Object, Map<String, Object>> cachedProperties = Maps.newHashMap();
    private String traceParams;

    public void addRealParam(int index, Object value) {
        realParams.add(Pair.of(index, value));
    }

    List<Pair<Integer, Integer>> outParameters = Lists.<Pair<Integer, Integer>>newArrayList();

    public void registerOutParameter(int index, int type) {
        outParameters.add(Pair.of(index, type));
    }

    public void bindParamsForEvaluation(String sqlClassPath) {
        createEvalSql(-1, sqlClassPath, eqlConfig, tagSqlId, boundParams.toString());
    }

    @SneakyThrows
    public void bindParams(PreparedStatement ps, String sqlClassPath) {
        for (Pair<Integer, Object> param : realParams) {
            ps.setObject(param._1, param._2);
        }
        for (Pair<Integer, Integer> out : outParameters) {
            ((CallableStatement) ps).registerOutParameter(out._1, out._2);
        }

        createEvalSql(-1, sqlClassPath, eqlConfig, tagSqlId, boundParams.toString());
    }

    @SneakyThrows
    public void bindBatchParams(PreparedStatement ps, int index, String sqlClassPath) {
        for (Pair<Integer, Object> param : realParams) {
            ps.setObject(param._1, ((Object[]) param._2)[index]);
        }

        createEvalSql(index, sqlClassPath, eqlConfig, tagSqlId, batchParamsString(boundParams, index));
    }

    private void createEvalSql(int index, String sqlClassPath, EqlConfigDecorator eqlConfig,
                               String tagSqlId, String msg) {
        boolean hasBoundParams = boundParams != null && boundParams.size() > 0;



        if (hasBoundParams) {
            Logger log = Logs.createLogger(eqlConfig, sqlClassPath, getSqlId(), tagSqlId, "params");
            log.debug(msg);
        }

        if (hasBoundParams) {
            Logger evalLog = Logs.createLogger(eqlConfig, sqlClassPath, getSqlId(), tagSqlId, "eval");
            /* if (isForEvaluate() || evalLog.isDebugEnabled()) */
            this.evalSql = parseEvalSql(index);
            evalLog.debug(this.evalSql);
        } else {
            this.evalSql = evalSqlTemplate;
        }

        this.traceParams = msg;
    }

    private String batchParamsString(List<Object> boundParams, int index) {
        ArrayList<Object> bounds = new ArrayList<Object>();
        for (Object object : boundParams) {
            bounds.add(((Object[]) object)[index]);
        }
        return bounds.toString();
    }

    public String getEvalSqlTemplate() {
        return evalSqlTemplate;
    }

    private String parseEvalSql(int batchIndex) {
        val eval = new StringBuilder();
        int startPos = 0;
        int index = -1;
        int size = boundParams.size();
        int evalSqlLength = evalSqlTemplate.length();

        val simpleDateFormat = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
        while (startPos < evalSqlLength) {
            String placeholder = S.wrap(++index, EqlParamsParser.SUB);
            int pos = evalSqlTemplate.indexOf(placeholder, startPos);
            if (pos < 0) break;

            eval.append(evalSqlTemplate.substring(startPos, pos));

            if (index < size) {
                Object boundParam = boundParams.get(index);
                if (batchIndex >= 0) {
                    boundParam = ((Object[]) boundParam)[batchIndex];
                }

                val evalBoundParam = createEvalBoundParam(simpleDateFormat, boundParam);
                eval.append(evalBoundParam);
            } else {
                eval.append('?');
            }

            startPos = pos + placeholder.length();
        }

        eval.append(evalSqlTemplate.substring(startPos));

        return eval.toString();
    }

    private String createEvalBoundParam(SimpleDateFormat simpleDateFormat, Object boundParam) {
        if (boundParam == null) return "NULL";
        if (boundParam instanceof Boolean)
            return (Boolean) boundParam ? "1" : "0";
        if (boundParam instanceof Number) return boundParam.toString();
        if (boundParam instanceof Date)
            return '\'' + simpleDateFormat.format((Date) boundParam) + '\'';
        if (boundParam instanceof byte[])
            return '\'' + Hex.encode((byte[]) boundParam) + '\'';

        return '\'' + S.escapeSingleQuotes(boundParam.toString()) + '\'';
    }

    @Getter String runSql;
    @Getter String printSql;
    @Setter @Getter Object result;
    @Setter @Getter EqlConfigDecorator eqlConfig;

    @Setter @Getter EqlBlock eqlBlock;
    @Setter @Getter int placeholderNum;
    @Getter EqlParamPlaceholder[] placeHolders;
    @Setter @Getter PlaceholderType placeHolderType;
    @Setter @Getter PlaceholderType placeHolderOutType;
    @Setter @Getter EqlType sqlType;
    @Setter @Getter boolean lastSelectSql;
    @Setter @Getter boolean willReturnOnlyOneRow;
    @Getter Object[] extraBindParams;
    @Setter @Getter EqlDynamic eqlDynamic;
    @Getter int outCount;

    @Setter @Getter Map<String, Object> executionContext;
    @Setter @Getter Object[] params;
    @Setter @Getter Object[] dynamics;
    @Setter @Getter Object paramBean;

    @Override @SneakyThrows
    public EqlRun clone() {
        return (EqlRun) super.clone();
    }

    private Object getDynamicsBean() {
        return dynamics == null || dynamics.length == 0 ? null : dynamics[0];
    }

    public void setRunSql(String runSql) {
        this.runSql = runSql;
        printSql = runSql.replaceAll("\\r?\\n", " ");
    }

    public String getSqlId() {
        if (S.isNotBlank(tagSqlId)) return tagSqlId;
        return eqlBlock != null ? eqlBlock.getSqlId() : "auto";
    }

    public void setPlaceHolders(EqlParamPlaceholder[] placeHolders) {
        this.placeHolders = placeHolders;
        outCount = 0;
        for (EqlParamPlaceholder placeHolder : placeHolders)
            if (placeHolder.getInOut() != EqlParamPlaceholder.InOut.IN)
                ++outCount;
    }

    public EqlParamPlaceholder getPlaceHolder(int index) {
        return index < placeHolders.length ? placeHolders[index] : null;
    }

    public void setExtraBindParams(Object... extraBindParams) {
        this.extraBindParams = extraBindParams;
    }

    public Map<String, Object> getMergedParamProperties() {
        return P.mergeProperties(executionContext, getParamBean());
    }

    public Map<String, Object> getMergedParamPropertiesWith(Object element) {
        return P.mergeProperties(executionContext, element);
    }

    public Object getIterateParams() {
        return ((Object[]) ((Map) getParamBean()).get("_params"))[0];
    }

    public Map<String, Object> getMergedDynamicsProperties() {
        return P.mergeProperties(executionContext, getDynamicsBean());
    }

    public void traceResult(Object execRet) {
        BlackcatUtils.trace(getSqlId(), printSql, traceParams, evalSql, execRet);
    }
}
