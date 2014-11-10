package org.n3r.eql.param;

import com.google.common.base.Objects;
import org.n3r.eql.base.ExpressionEvaluator;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.Names;
import org.n3r.eql.util.S;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EqlParamsBinder {
    private EqlRun eqlRun;
    private List<Object> boundParams;

    private static enum ParamExtra {
        Extra, Normal
    }

    public void prepareBindParams(EqlRun eqlRun) {
        this.eqlRun = eqlRun;
        boundParams = new ArrayList<Object>();

        switch (eqlRun.getPlaceHolderType()) {
            case AUTO_SEQ:
                for (int i = 0; i < eqlRun.getPlaceholderNum(); ++i)
                    setParam(i, getParamByIndex(i), ParamExtra.Normal);
                break;
            case MANU_SEQ:
                for (int i = 0; i < eqlRun.getPlaceholderNum(); ++i)
                    setParam(i, findParamBySeq(i + 1), ParamExtra.Normal);
                break;
            case VAR_NAME:
                for (int i = 0; i < eqlRun.getPlaceholderNum(); ++i)
                    setParam(i, findParamByName(i), ParamExtra.Normal);
                break;
            default:
                break;
        }

        bindExtraParams();

        eqlRun.setBoundParams(boundParams);
    }

    private void bindExtraParams() {
        Object[] extraBindParams = eqlRun.getExtraBindParams();
        if (extraBindParams == null) return;

        for (int i = eqlRun.getPlaceholderNum(); i < eqlRun.getPlaceholderNum() + extraBindParams.length; ++i)
            setParam(i, extraBindParams[i - eqlRun.getPlaceholderNum()], ParamExtra.Extra);
    }

    private void setParam(int index, Object value, ParamExtra extra) {
        EqlParamPlaceholder placeHolder = eqlRun.getPlaceHolder(index);
        try {
            switch (extra) {
                case Extra:
                    setParamValue(placeHolder, index, value);
                    break;
                default:
                    setParamEx(placeHolder, index, value);
                    break;
            }
        } catch (SQLException e) {
            throw new EqlExecuteException("set parameters fail", e);
        }
    }

    private void setParamValue(EqlParamPlaceholder placeHolder, int index, Object value) throws SQLException {
        Object boundParam = value;
        Object paramValue = value;
        if (value == null) {
            // nothing to do
        } else if (value instanceof Date) {
            Timestamp date = new Timestamp(((Date) value).getTime());
            boundParam = S.toDateTimeStr(date);
            paramValue = date;
        } else if (value.getClass().isEnum()) {
            if (value instanceof InternalValueable) {
                paramValue = ((InternalValueable) value).internalValue();
                boundParam = paramValue;
            }
        } else {
            if (placeHolder != null && value instanceof CharSequence) {
                String strValue = value.toString();
                if (placeHolder.isLob()) {
                    paramValue = S.toBytes(strValue);
                } else if (placeHolder.getLike() == EqlParamPlaceholder.Like.Like) {
                    paramValue = tryAddLeftAndRightPercent(strValue);
                } else if (placeHolder.getLike() == EqlParamPlaceholder.Like.RightLike) {
                    paramValue = tryAddRightPercent(strValue);
                } else if (placeHolder.getLike() == EqlParamPlaceholder.Like.LeftLike) {
                    paramValue = tryAddLeftPercent(strValue);
                } else if (placeHolder.isNumberColumn() && strValue.trim().length() == 0) {
                    paramValue = null;
                }
            }

            boundParam = paramValue;
        }

        boundParams.add(boundParam);
        eqlRun.addRealParam(index + 1, paramValue);
    }

    private String tryAddLeftPercent(String strValue) {
        return (strValue.startsWith("%") ? "" : "%") + strValue;
    }

    private String tryAddRightPercent(String strValue) {
        return strValue + (strValue.endsWith("%") ? "" : "%");
    }

    private String tryAddLeftAndRightPercent(String strValue) {
        return (strValue.startsWith("%") ? "" : "%") + strValue + (strValue.endsWith("%") ? "" : "%");
    }

    private void setParamEx(EqlParamPlaceholder placeHolder, int index, Object value) throws SQLException {
        if (registerOut(index)) return;

        setParamValue(placeHolder, index, value);
    }

    private boolean registerOut(int index) throws SQLException {
        EqlParamPlaceholder.InOut inOut = eqlRun.getPlaceHolders()[index].getInOut();
        if (eqlRun.getSqlType().isProcedure() && inOut != EqlParamPlaceholder.InOut.IN) {
            // ((CallableStatement) ps).registerOutParameter(index + 1, Types.VARCHAR);
            eqlRun.registerOutParameter(index + 1, Types.VARCHAR);
        }

        return inOut == EqlParamPlaceholder.InOut.OUT;
    }

    private Object findParamByName(int index) {
        String varName = eqlRun.getPlaceHolders()[index].getPlaceholder();

        ExpressionEvaluator evaluator = eqlRun.getEqlConfig().getExpressionEvaluator();

        Object property = evaluator.eval(varName, eqlRun);

        if (property != null) return property;

        String propertyName = Names.underscoreNameToPropertyName(varName);
        return Objects.equal(propertyName, varName) ? property : evaluator.eval(propertyName, eqlRun);
    }


    private Object getParamByIndex(int index) {
        EqlParamPlaceholder[] placeHolders = eqlRun.getPlaceHolders();
        if (index < placeHolders.length && eqlRun.getSqlType().isProcedure()
                && placeHolders[index].getInOut() == EqlParamPlaceholder.InOut.OUT) return null;

        Object[] params = eqlRun.getParams();
        if (params != null && index < params.length)
            return params[index];

        throw new EqlExecuteException("[" + eqlRun.getSqlId() + "] lack parameters at runtime");
    }

    private Object findParamBySeq(int index) {
        return getParamByIndex(eqlRun.getPlaceHolders()[index - 1].getSeq() - 1);
    }
}
