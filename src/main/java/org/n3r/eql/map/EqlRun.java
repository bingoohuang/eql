package org.n3r.eql.map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.n3r.eql.config.EqlConfigDecorator;
import org.n3r.eql.param.EqlParamPlaceholder;
import org.n3r.eql.param.EqlParamsParser;
import org.n3r.eql.param.PlaceholderType;
import org.n3r.eql.parser.EqlBlock;
import org.n3r.eql.util.*;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class EqlRun implements Cloneable {
    public List<Pair<Integer, Object>> realParams = Lists.newArrayList();

    @Setter List<Object> boundParams;
    @Getter @Setter Connection connection;
    @Getter String evalSql;
    @Getter @Setter String evalSqlTemplate;
    @Setter @Getter EqlDynamic evalEqlDynamic;
    @Setter @Getter boolean iterateOption;
    @Setter @Getter String tagSqlId;
    @Setter @Getter boolean forEvaluate;
    @Getter Map<Object, Map<String, Object>> cachedProperties = Maps.newHashMap();
    private String traceParams;

    public void addRealParam(int index, Object value) {
        realParams.add(Pair.of(index, value));
    }

    List<Pair<Integer, Integer>> outParameters = Lists.newArrayList();

    public void registerOutParameter(int index, int type) {
        outParameters.add(Pair.of(index, type));
    }

    public void bindParamsForEvaluation(String sqlClassPath) {
        createEvalSql(-1, sqlClassPath, eqlConfig, tagSqlId, boundParams.toString());
    }

    public static final boolean HasJodaDateTime = BlackcatUtils.classExists("org.joda.time.DateTime");

    @SneakyThrows
    public void setParam(PreparedStatement ps, int parameterIndex, Object parameterValue) {
        if (HasJodaDateTime) {
            if (parameterValue instanceof DateTime) {
                val dateTime = (DateTime) parameterValue;
                ps.setObject(parameterIndex, new Timestamp(dateTime.getMillis()));
                return;
            }
            if (parameterValue instanceof LocalDate) {
                val jodaDate = (LocalDate) parameterValue;
                ps.setObject(parameterIndex, java.sql.Date.valueOf(
                        java.time.LocalDate.of(jodaDate.getYear(),jodaDate.getMonthOfYear(),jodaDate.getDayOfMonth())));
                return;
            }
            if (parameterValue instanceof LocalTime) {
                val jodaTime = (LocalTime) parameterValue;
                ps.setObject(parameterIndex, Time.valueOf(
                        java.time.LocalTime.of(
                                jodaTime.getHourOfDay(), jodaTime.getMinuteOfHour(), jodaTime.getSecondOfMinute(), jodaTime.getMillisOfSecond())));
                return;
            }
        }

        ps.setObject(parameterIndex, parameterValue);
    }

    @SneakyThrows
    public void bindParams(PreparedStatement ps, String sqlClassPath) {
        for (val param : realParams) {
            setParam(ps, param._1, param._2);
        }
        for (val out : outParameters) {
            val cs = (CallableStatement) ps;
            cs.registerOutParameter(out._1, out._2);
        }

        createEvalSql(-1, sqlClassPath, eqlConfig, tagSqlId, boundParams.toString());
    }

    @SneakyThrows
    public void bindBatchParams(PreparedStatement ps, int index, String sqlClassPath) {
        for (val param : realParams) {
            val x = ((Object[]) param._2)[index];
            setParam(ps, param._1, x);
        }

        createEvalSql(index, sqlClassPath, eqlConfig, tagSqlId, batchParamsString(boundParams, index));
    }

    private void createEvalSql(int index, String sqlClassPath, EqlConfigDecorator eqlConfig,
                               String tagSqlId, String msg) {
        val hasBoundParams = boundParams != null && boundParams.size() > 0;

        if (hasBoundParams) {
            val log = Logs.createLogger(eqlConfig, sqlClassPath, getSqlId(), tagSqlId, "params");
            log.debug(msg);
        }

        if (hasBoundParams) {
            val evalLog = Logs.createLogger(eqlConfig, sqlClassPath, getSqlId(), tagSqlId, "eval");
            /* if (isForEvaluate() || evalLog.isDebugEnabled()) */
            this.evalSql = parseEvalSql(index);
            evalLog.debug(this.evalSql);
        } else {
            this.evalSql = evalSqlTemplate;
        }

        this.traceParams = msg;
    }

    private String batchParamsString(List<Object> boundParams, int index) {
        List<Object> bounds = Lists.newArrayList();
        for (val object : boundParams) {
            bounds.add(((Object[]) object)[index]);
        }
        return bounds.toString();
    }

    private String parseEvalSql(int batchIndex) {
        val eval = new StringBuilder();
        int startPos = 0;
        int index = -1;
        int size = boundParams.size();
        int evalSqlLength = evalSqlTemplate.length();

        val simpleDateFormat = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
        while (startPos < evalSqlLength) {
            val placeholder = S.wrap(++index, EqlParamsParser.SUB);
            int pos = evalSqlTemplate.indexOf(placeholder, startPos);
            if (pos < 0) break;

            eval.append(evalSqlTemplate, startPos, pos);

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

    @Override
    @SneakyThrows
    public EqlRun clone() {
        return (EqlRun) super.clone();
    }

    private Object getDynamicsBean() {
        return dynamics == null || dynamics.length == 0 ? null : dynamics[0];
    }


    static Pattern WHERE_PATTERN = Pattern.compile("\\bwhere\\b", Pattern.CASE_INSENSITIVE);
    public void setRunSql(String runSql) {
        this.runSql = runSql;
        printSql = runSql.replaceAll("\\r?\\n", " ");

        checkNoWhereUpdate(runSql);
    }

    private void checkNoWhereUpdate(String runSql) {
        if (!sqlType.isUpdateDeleteStmt()) return;
        if (WHERE_PATTERN.matcher(runSql).find()) return;
        if (eqlBlock.getOptions().containsKey("NoWhere")) return;

        throw new RuntimeException("where clause is required when there is no NoWhere option with the sql " + runSql);
    }

    public String getSqlId() {
        if (S.isNotBlank(tagSqlId)) return tagSqlId;
        return eqlBlock != null ? eqlBlock.getSqlId() : "auto";
    }

    public void setPlaceHolders(EqlParamPlaceholder[] placeHolders) {
        this.placeHolders = placeHolders;
        outCount = 0;
        for (val placeHolder : placeHolders) {
            if (placeHolder.getInOut() != EqlParamPlaceholder.InOut.IN)
                ++outCount;
        }
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
