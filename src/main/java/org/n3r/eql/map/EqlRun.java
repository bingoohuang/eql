package org.n3r.eql.map;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.n3r.eql.config.EqlConfigDecorator;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.param.EqlParamPlaceholder;
import org.n3r.eql.param.EqlParamsParser;
import org.n3r.eql.param.PlaceholderType;
import org.n3r.eql.parser.EqlBlock;
import org.n3r.eql.util.Hex;
import org.n3r.eql.util.P;
import org.n3r.eql.util.Pair;
import org.n3r.eql.util.S;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EqlRun implements Cloneable {
    public List<Pair<Integer, Object>> realParams = Lists.newArrayList();
    private List<Object> boundParams;
    private Connection connection;
    private String evalSql;
    private String evalSqlTemplate;
    private EqlDynamic evalEqlDynamic;
    private boolean batchOption;

    public void addRealParam(int index, Object value) {
        realParams.add(Pair.of(index, value));
    }

    List<Pair<Integer, Integer>> outParameters = Lists.newArrayList();

    public void registerOutParameter(int index, int type) {
        outParameters.add(Pair.of(index, type));
    }

    public void setBoundParams(List<Object> boundParams) {
        this.boundParams = boundParams;
    }

    Logger logger = LoggerFactory.getLogger(EqlRun.class);

    public void bindParams(PreparedStatement ps) {
        try {
            for (Pair<Integer, Object> param : realParams) {
                ps.setObject(param._1, param._2);
            }
            for (Pair<Integer, Integer> out : outParameters) {
                ((CallableStatement) ps).registerOutParameter(out._1, out._2);
            }
        } catch (SQLException e) {
            throw new EqlExecuteException(e);
        }

        if (boundParams != null && boundParams.size() > 0 && logger.isDebugEnabled()) {
            String sqlId = getSqlId();
            logger.debug("params for [{}]: {}", sqlId, boundParams.toString());
            this.evalSql  = parseEvalSql(-1);
            logger.debug("eval sql for [{}]: {}", sqlId, this.evalSql );
        }
    }

    public void bindBatchParams(PreparedStatement ps, int index) {
        try {
            for (Pair<Integer, Object> param : realParams) {
                ps.setObject(param._1, ((Object[]) param._2)[index]);
            }
        } catch (SQLException e) {
            throw new EqlExecuteException(e);
        }

        if (boundParams != null && boundParams.size() > 0 && logger.isDebugEnabled()) {
            String sqlId = getSqlId();
            logger.debug("params for [{}]: {}", sqlId, batchParamsString(boundParams, index));
            this.evalSql  = parseEvalSql(index);
            logger.debug("eval sql for [{}]: {}", sqlId, this.evalSql );
        }
    }

    private String batchParamsString(List<Object> boundParams, int index) {
        ArrayList<Object> bounds = new ArrayList<Object>();
        for (Object object: boundParams) {
            bounds.add(((Object[])object)[index]);
        }
        return bounds.toString();
    }

    public String getEvalSqlTemplate() {
        return evalSqlTemplate;
    }

    private String parseEvalSql(int batchIndex) {
        StringBuilder eval = new StringBuilder();
        int startPos = 0;
        int index = -1;
        int size = boundParams.size();
        int evalSqlLength = evalSqlTemplate.length();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
        while (startPos < evalSqlLength) {
            String placeholder = S.wrap(++index, EqlParamsParser.SUB);
            int pos = evalSqlTemplate.indexOf(placeholder, startPos);
            if (pos < 0) break;

            eval.append(evalSqlTemplate.substring(startPos, pos));
            if (index < size) {
                Object boundParam = boundParams.get(index);
                if (batchIndex >= 0) {
                    boundParam = ((Object[])boundParam)[batchIndex];
                }

                if (boundParam == null) {
                    eval.append("NULL");
                } else if (boundParam instanceof Number) {
                    eval.append(boundParam.toString());
                } else if (boundParam instanceof java.util.Date) {
                    String formattedDate = simpleDateFormat.format((Date) boundParam);
                    eval.append('\'').append(formattedDate).append('\'');
                } else if (boundParam instanceof byte[]) {
                    String hexParam = Hex.encode((byte[]) boundParam);
                    eval.append('\'').append(hexParam).append('\'');
                } else {
                    String escapedParam = S.escapeSingleQuotes(boundParam.toString());
                    eval.append('\'').append(escapedParam).append('\'');
                }
            } else {
                eval.append('?');
            }

            startPos = pos + placeholder.length();
        }

        eval.append(evalSqlTemplate.substring(startPos));

        return eval.toString();
    }


    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    private String runSql;
    private String printSql;
    private Object result;
    private EqlConfigDecorator eqlConfig;

    private EqlBlock eqlBlock;
    private int placeholderNum;
    private EqlParamPlaceholder[] placeHolders;
    private PlaceholderType placeHolderType;
    private PlaceholderType placeHolderOutType;
    private EqlType sqlType;
    private boolean lastSelectSql;
    private boolean willReturnOnlyOneRow;
    private Object[] extraBindParams;
    private EqlDynamic eqlDynamic;
    private int outCount;

    private Map<String, Object> executionContext;
    private Object[] params;
    private Object[] dynamics;
    private Object paramBean;

    @Override
    public EqlRun clone() {
        try {
            return (EqlRun) super.clone();
        } catch (CloneNotSupportedException e) {
            throw Throwables.propagate(e);
        }
    }

    public EqlConfigDecorator getEqlConfig() {
        return eqlConfig;
    }

    public void setEqlConfig(EqlConfigDecorator eqlConfig) {
        this.eqlConfig = eqlConfig;
    }

    public void setParamBean(Object paramBean) {
        this.paramBean = paramBean;
    }

    public Object getParamBean() {
        return paramBean;
    }

    public void setDynamics(Object[] dynamics) {
        this.dynamics = dynamics;
    }

    public Object[] getDynamics() {
        return dynamics;
    }

    private Object getDynamicsBean() {
        return dynamics == null || dynamics.length == 0 ? null : dynamics[0];
    }

    public void setExecutionContext(Map<String, Object> executionContext) {
        this.executionContext = executionContext;
    }

    public Map<String, Object> getExecutionContext() {
        return executionContext;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public Object[] getParams() {
        return params;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Object getResult() {
        return result;
    }

    public String getPrintSql() {
        return printSql;
    }

    public void setRunSql(String runSql) {
        this.runSql = runSql;
        printSql = runSql.replaceAll("\\r?\\n", " ");
    }

    public String getRunSql() {
        return runSql;
    }

    public String getSqlId() {
        return eqlBlock != null ? eqlBlock.getSqlId() : "<AUTO>";
    }

    public void setPlaceHolders(EqlParamPlaceholder[] placeHolders) {
        this.placeHolders = placeHolders;
        outCount = 0;
        for (EqlParamPlaceholder placeHolder : placeHolders)
            if (placeHolder.getInOut() != EqlParamPlaceholder.InOut.IN) ++outCount;
    }

    public EqlParamPlaceholder[] getPlaceHolders() {
        return placeHolders;
    }

    public EqlParamPlaceholder getPlaceHolder(int index) {
        return index < placeHolders.length ? placeHolders[index] : null;
    }

    public void setPlaceHolderType(PlaceholderType placeHolderType) {
        this.placeHolderType = placeHolderType;
    }

    public PlaceholderType getPlaceHolderType() {
        return placeHolderType;
    }

    public int getPlaceholderNum() {
        return placeholderNum;
    }

    public void setPlaceholderNum(int placeholderNum) {
        this.placeholderNum = placeholderNum;
    }

    public EqlBlock getEqlBlock() {
        return eqlBlock;
    }

    public void setEqlBlock(EqlBlock eqlBlock) {
        this.eqlBlock = eqlBlock;
    }

    public EqlType getSqlType() {
        return sqlType;
    }

    public void setSqlType(EqlType sqlType) {
        this.sqlType = sqlType;
    }

    public void setLastSelectSql(boolean lastSelectSql) {
        this.lastSelectSql = lastSelectSql;
    }

    public boolean isLastSelectSql() {
        return lastSelectSql;
    }

    public boolean isWillReturnOnlyOneRow() {
        return willReturnOnlyOneRow;
    }

    public void setWillReturnOnlyOneRow(boolean willReturnOnlyOneRow) {
        this.willReturnOnlyOneRow = willReturnOnlyOneRow;
    }

    public Object[] getExtraBindParams() {
        return extraBindParams;
    }

    public void setExtraBindParams(Object... extraBindParams) {
        this.extraBindParams = extraBindParams;
    }

    public void setEqlDynamic(EqlDynamic eqlDynamic) {
        this.eqlDynamic = eqlDynamic;
    }

    public EqlDynamic getEqlDynamic() {
        return eqlDynamic;
    }

    public int getOutCount() {
        return outCount;
    }

    public PlaceholderType getPlaceHolderOutType() {
        return placeHolderOutType;
    }

    public void setPlaceHolderOutType(PlaceholderType placeHolderOutType) {
        this.placeHolderOutType = placeHolderOutType;
    }

    public Map<String, Object> getMergedParamProperties() {
        return P.mergeProperties(executionContext, getParamBean());
    }


    public Map<String, Object> getMergedParamPropertiesWith(Object element) {
        return P.mergeProperties(executionContext, element);
    }

    public Collection<Object> getBatchCollectionParams() {
        return (Collection<Object>) ((Object[])((Map)getParamBean()).get("_params"))[0];
    }

    public Map<String, Object> getMergedDynamicsProperties() {
        return P.mergeProperties(executionContext, getDynamicsBean());
    }

    public void setEvalSqlTemplate(String evalSqlTemplate) {
        this.evalSqlTemplate = evalSqlTemplate;
    }

    public void setEvalEqlDynamic(EqlDynamic evalEqlDynamic) {
        this.evalEqlDynamic = evalEqlDynamic;
    }

    public EqlDynamic getEvalEqlDynamic() {
        return evalEqlDynamic;
    }

    public void setBatchOption(boolean batchOption) {
        this.batchOption = batchOption;
    }

    public boolean hasBatchOption() {
        return batchOption;
    }


    public String getEvalSql() {
        return evalSql;
    }
}
