package org.n3r.eql.param;

import com.google.common.base.Objects;
import lombok.SneakyThrows;
import lombok.val;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.param.EqlParamPlaceholder.InOut;
import org.n3r.eql.util.Names;

import java.util.ArrayList;
import java.util.List;

public class EqlParamsBinder {
    private EqlRun eqlRun;
    private List<Object> boundParams;
    private boolean hasIterateOption;

    private enum ParamExtra {
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

        int i = eqlRun.getPlaceholderNum();
        int ii = eqlRun.getPlaceholderNum() + extraBindParams.length;
        for (; i < ii; ++i) {
            val extraParam = extraBindParams[i - eqlRun.getPlaceholderNum()];
            setParam(i, extraParam, ParamExtra.Extra);
        }
    }

    private void setParam(int index, Object value, ParamExtra extra) {
        val placeHolder = eqlRun.getPlaceHolder(index);
        switch (extra) {
            case Extra:
                setParamValue(placeHolder, index, value);
                break;
            default:
                setParamEx(placeHolder, index, value);
                break;
        }
    }

    @SneakyThrows
    private void setParamValue(EqlParamPlaceholder placeHolder, int index, Object value) {
        if (hasIterateOption) {
            List<Object> values = (List<Object>) value;
            Object[] boundParam = new Object[values.size()];
            Object[] paramsValue = new Object[boundParam.length];

            for (int i = 0, ii = boundParam.length; i < ii; ++i) {
                val paramValueDealer = new ParamValueDealer(placeHolder);
                paramValueDealer.dealSingleValue(values.get(i));
                boundParam[i] = paramValueDealer.getBoundParam();
                paramsValue[i] = paramValueDealer.getParamValue();
            }

            boundParams.add(boundParam);
            eqlRun.addRealParam(index + 1, paramsValue);
        } else {
            val paramValDealer = new ParamValueDealer(placeHolder);
            paramValDealer.dealSingleValue(value);
            boundParams.add(paramValDealer.getBoundParam());
            eqlRun.addRealParam(index + 1, paramValDealer.getParamValue());
        }
    }

    private void setParamEx(EqlParamPlaceholder placeHolder, int index, Object value) {
        if (registerOut(index)) return;

        setParamValue(placeHolder, index, value);
    }

    private boolean registerOut(int index) {
        EqlParamPlaceholder placeholder = eqlRun.getPlaceHolders()[index];
        val inOut = placeholder.getInOut();
        if (eqlRun.getSqlType().isProcedure() && inOut != InOut.IN) {
            eqlRun.registerOutParameter(index + 1, placeholder.getOutType());
        }

        return inOut == InOut.OUT;
    }

    private Object findParamByName(int index) {
        String varName = eqlRun.getPlaceHolders()[index].getPlaceholder();

        val evaluator = eqlRun.getEqlConfig().getExpressionEvaluator();

        Object property = evaluator.eval(varName, eqlRun);

        if (!hasIterateOption && property != null
                || hasIterateOption && !isAllNullInBatchOption(property))
            return property;

        String propertyName = Names.underscoreNameToPropertyName(varName);
        return Objects.equal(propertyName, varName) ? property : evaluator.eval(propertyName, eqlRun);
    }

    private boolean isAllNullInBatchOption(Object property) {
        val listProperties = (List<Object>) property;
        for (Object object : listProperties) {
            if (object != null) return false;
        }

        return true;
    }


    private Object getParamByIndex(int index) {
        val placeHolders = eqlRun.getPlaceHolders();
        if (index < placeHolders.length && eqlRun.getSqlType().isProcedure()
                && placeHolders[index].getInOut() == InOut.OUT)
            return null;

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
