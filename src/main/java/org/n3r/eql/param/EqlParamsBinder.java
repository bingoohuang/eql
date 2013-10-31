package org.n3r.eql.param;

import com.google.common.base.Objects;
import ognl.Ognl;
import ognl.OgnlException;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.util.EqlUtils;
import org.n3r.eql.map.EqlRun;
import org.slf4j.Logger;

import java.sql.*;
import java.util.Date;

public class EqlParamsBinder {
    private EqlRun subSql;
    private Object[] params;
    private StringBuilder boundParams;
    private PreparedStatement ps;

    private static enum ParamExtra {
        Extra, Normal
    }

    public void bindParams(PreparedStatement ps, EqlRun subSql, Object[] params, Logger logger) {
        this.subSql = subSql;
        this.params = params;
        boundParams = new StringBuilder();
        this.ps = ps;

        switch (subSql.getPlaceHolderType()) {
            case AUTO_SEQ:
                for (int i = 0; i < subSql.getPlaceholderNum(); ++i)
                    setParam(subSql.getPlaceHolder(i), i, getParamByIndex(i), ParamExtra.Normal);
                break;
            case MANU_SEQ:
                for (int i = 0; i < subSql.getPlaceholderNum(); ++i)
                    setParam(subSql.getPlaceHolder(i), i, findParamBySeq(i + 1), ParamExtra.Normal);
                break;
            case VAR_NAME:
                for (int i = 0; i < subSql.getPlaceholderNum(); ++i)
                    setParam(subSql.getPlaceHolder(i), i, findParamByName(subSql, i), ParamExtra.Normal);
                break;
            default:
                break;
        }

        bindExtraParams();

        if (boundParams.length() > 0) logger.debug("param: {}", boundParams);
    }

    private void bindExtraParams() {
        Object[] extraBindParams = subSql.getExtraBindParams();
        if (extraBindParams == null) return;

        for (int i = subSql.getPlaceholderNum(); i < subSql.getPlaceholderNum() + extraBindParams.length; ++i)
            setParam(subSql.getPlaceHolder(i), i, extraBindParams[i - subSql.getPlaceholderNum()], ParamExtra.Extra);
    }

    private void setParam(EqlParamPlaceholder placeHolder, int index, Object value, ParamExtra extra) {
        try {
            switch (extra) {
                case Extra:
                    setParamExtra(placeHolder, index, value);
                    break;
                default:
                    setParamEx(placeHolder, index, value);
                    break;
            }
        } catch (SQLException e) {
            throw new EqlExecuteException("set parameters fail", e);
        }
    }

    private void setParamExtra(EqlParamPlaceholder placeHolder, int index, Object value) throws SQLException {
        if (value instanceof Date) {
            Timestamp date = new Timestamp(((Date) value).getTime());
            ps.setTimestamp(index + 1, date);
            boundParams.append('[').append(EqlUtils.toDateTimeStr(date)).append(']');
        } else {
            Object paramValue = value;
            if (placeHolder != null && placeHolder.getOption().contains("LOB") && value instanceof String)
                paramValue = EqlUtils.toBytes((String) value);

            ps.setObject(index + 1, paramValue);
            boundParams.append('[').append(value).append(']');
        }
    }

    private void setParamEx(EqlParamPlaceholder placeHolder, int index, Object value) throws SQLException {
        if (regiesterOut(index)) return;

        setParamExtra(placeHolder, index, value);
    }

    private boolean regiesterOut(int index) throws SQLException {
        EqlParamPlaceholder.InOut inOut = subSql.getPlaceHolders()[index].getInOut();
        if (EqlUtils.isProcedure(subSql.getSqlType()) && inOut != EqlParamPlaceholder.InOut.IN)
            ((CallableStatement) ps).registerOutParameter(index + 1, Types.VARCHAR);

        return inOut == EqlParamPlaceholder.InOut.OUT;
    }

    private Object findParamByName(EqlRun subSql, int index) {
        Object bean = EqlUtils.compositeParams(params);

        String varName = subSql.getPlaceHolders()[index].getPlaceholder();
        // Object property = RBean.getPropertyQuietly(bean, varName);
        Object property = getPropertyValue(bean, varName);

        if (property != null) return property;

        String propertyName = EqlUtils.convertUnderscoreNameToPropertyName(varName);
        if (!Objects.equal(propertyName, varName))
            property = getPropertyValue(bean, propertyName); // RBean.getPropertyQuietly(bean, propertyName);

        return property;
    }

    private Object getPropertyValue(Object bean, String varName) {
        try {
            return Ognl.getValue(varName, bean);
        } catch (OgnlException e) {
            throw new RuntimeException(e);
        }
    }

    private Object getParamByIndex(int index) {
        EqlParamPlaceholder[] placeHolders = subSql.getPlaceHolders();
        if (index < placeHolders.length && EqlUtils.isProcedure(subSql.getSqlType())
                && placeHolders[index].getInOut() == EqlParamPlaceholder.InOut.OUT) return null;

        if (params != null && index < params.length)
            return params[index];

        throw new EqlExecuteException("[" + subSql.getSqlId() + "]执行过程中缺少参数");
    }

    private Object findParamBySeq(int index) {
        return getParamByIndex(subSql.getPlaceHolders()[index - 1].getSeq() - 1);
    }


}
