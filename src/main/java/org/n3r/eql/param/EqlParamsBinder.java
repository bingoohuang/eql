package org.n3r.eql.param;

import com.google.common.base.Objects;
import org.n3r.eql.base.ExpressionEvaluator;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.Names;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class EqlParamsBinder {
    private EqlRun eqlRun;
    private List<Object> boundParams;
    private boolean hasIterateOption;

    private static enum ParamExtra {
        Extra, Normal
    }

    public void prepareBindParams(boolean hasIterateOption, EqlRun eqlRun) {
        this.hasIterateOption = hasIterateOption;
        this.eqlRun = eqlRun;

        eqlRun.setIterateOption(hasIterateOption);
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

        if (hasIterateOption) {
            List<Object> values = (List<Object>) value;
            Object[] boundParam = new Object[values.size()];
            Object[] paramsValue = new Object[boundParam.length];

            for (int i = 0, ii = boundParam.length; i < ii; ++i) {
                ParamValueDealer paramValueDealer = new ParamValueDealer(placeHolder);
                paramValueDealer.dealSingleValue(values.get(i));
                boundParam[i] = paramValueDealer.getBoundParam();
                paramsValue[i] = paramValueDealer.getParamValue();
            }

            boundParams.add(boundParam);
            eqlRun.addRealParam(index + 1, paramsValue);
        } else {
            ParamValueDealer paramValueDealer = new ParamValueDealer(placeHolder);
            paramValueDealer.dealSingleValue(value);
            boundParams.add(paramValueDealer.getBoundParam());
            eqlRun.addRealParam(index + 1, paramValueDealer.getParamValue());
        }
    }


    private void setParamEx(EqlParamPlaceholder placeHolder, int index, Object value) throws SQLException {
        if (registerOut(index)) return;

        setParamValue(placeHolder, index, value);
    }

    private boolean registerOut(int index) throws SQLException {
        EqlParamPlaceholder.InOut inOut = eqlRun.getPlaceHolders()[index].getInOut();
        if (eqlRun.getSqlType().isProcedure() && inOut != EqlParamPlaceholder.InOut.IN) {
            eqlRun.registerOutParameter(index + 1, Types.VARCHAR);
        }

        return inOut == EqlParamPlaceholder.InOut.OUT;
    }

    private Object findParamByName(int index) {
        String varName = eqlRun.getPlaceHolders()[index].getPlaceholder();

        ExpressionEvaluator evaluator = eqlRun.getEqlConfig().getExpressionEvaluator();

        Object property = evaluator.eval(varName, eqlRun);

        if (!hasIterateOption && property != null
                || hasIterateOption && !isAllNullInBatchOption(property)) return property;

        String propertyName = Names.underscoreNameToPropertyName(varName);
        return Objects.equal(propertyName, varName) ? property : evaluator.eval(propertyName, eqlRun);
    }

    private boolean isAllNullInBatchOption(Object property) {
        List<Object> listProperties = (List<Object>) property;
        for (Object object : listProperties) {
            if (object != null) return false;
        }

        return true;
    }


    private Object getParamByIndex(int index) {
        EqlParamPlaceholder[] placeHolders = eqlRun.getPlaceHolders();
        if (index < placeHolders.length && eqlRun.getSqlType().isProcedure()
                && placeHolders[index].getInOut() == EqlParamPlaceholder.InOut.OUT) return null;

        if (hasIterateOption)
            throw new EqlExecuteException("bad parameters when batch option is set");

        Object[] params = eqlRun.getParams();
        if (params != null && index < params.length)
            return params[index];

        throw new EqlExecuteException("[" + eqlRun.getSqlId() + "] lack parameters at runtime");
    }

    private Object findParamBySeq(int index) {
        return getParamByIndex(eqlRun.getPlaceHolders()[index - 1].getSeq() - 1);
    }
}
