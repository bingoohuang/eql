package org.n3r.eql.param;

import com.google.common.base.Objects;
import lombok.SneakyThrows;
import lombok.val;
import org.n3r.eql.EqlContext;
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
            case UNSET: // Only occurs when all parameters are optioned by :context or :contextOnly
                for (int i = 0; i < eqlRun.getPlaceholderNum(); ++i) {
                    val placeHolders = eqlRun.getPlaceHolders();
                    val holder = placeHolders[i];

                    Object param = null;
                    if (holder.hasContextNormal()) {
                        param = parseVarValue(holder);
                    }
                    if (param == null) {
                        param = EqlContext.get(holder.getContextName());
                    }

                    setParam(i, param, ParamExtra.Normal);
                }
                break;
            case AUTO_SEQ:
                for (int i = 0; i < eqlRun.getPlaceholderNum(); ++i) {
                    Object param = getParamByIndex(i);
                    setParam(i, param, ParamExtra.Normal);
                }
                break;
            case MANU_SEQ:
                for (int i = 0; i < eqlRun.getPlaceholderNum(); ++i)
                    setParam(i, findParamBySeq(i), ParamExtra.Normal);
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
        if (extra == ParamExtra.Extra) {
            setParamValue(placeHolder, index, value);
        } else {
            setParamEx(placeHolder, index, value);
        }
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private void setParamValue(EqlParamPlaceholder placeHolder, int index, Object value) {
        if (hasIterateOption) {
            val values = (List<Object>) value;
            val boundParam = new Object[values.size()];
            val paramsValue = new Object[boundParam.length];

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
        val placeholder = eqlRun.getPlaceHolders()[index];
        val inOut = placeholder.getInOut();
        if (eqlRun.getSqlType().isProcedure() && inOut != InOut.IN) {
            eqlRun.registerOutParameter(index + 1, placeholder.getOutType());
        }

        return inOut == InOut.OUT;
    }

    private Object findParamByName(int index) {
        val placeholder = eqlRun.getPlaceHolders()[index];
        if (placeholder.hasContextOnly()) {
            return EqlContext.get(placeholder.getContextName());
        }

        val varValue = parseVarValue(placeholder);
        if (varValue != null) {
            return varValue;
        }

        if (placeholder.hasContextNormal()) {
            return EqlContext.get(placeholder.getContextName());
        }

        return null;
    }

    private Object parseVarValue(EqlParamPlaceholder placeholder) {
        val varName = placeholder.getPlaceholder();
        val evaluator = eqlRun.getEqlConfig().getExpressionEvaluator();
        val property = evaluator.eval(varName, eqlRun);

        if (!hasIterateOption && property != null
                || hasIterateOption && !isAllNullInBatchOption(property))
            return property;

        val propName = Names.underscoreNameToPropertyName(varName);
        return Objects.equal(propName, varName)
                ? property : evaluator.eval(propName, eqlRun);
    }


    @SuppressWarnings("unchecked")
    private boolean isAllNullInBatchOption(Object property) {
        for (val object : (List<Object>) property) {
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

        val placeholder = placeHolders[index];
        if (placeholder.hasContextOnly()) {
            return EqlContext.get(placeholder.getContextName());
        }

        int offset = computeOffset(index);

        val params = eqlRun.getParams();
        if (params != null && index - offset < params.length)
            return params[index - offset];

        throw new EqlExecuteException("[" + eqlRun.getSqlId() + "] lack parameters at runtime");
    }

    private Object getParamBySeq(EqlParamPlaceholder placeholder, int seq) {
        val placeHolders = eqlRun.getPlaceHolders();
        if (seq < placeHolders.length && eqlRun.getSqlType().isProcedure()
                && placeholder.getInOut() == InOut.OUT)
            return null;

        if (hasIterateOption)
            throw new EqlExecuteException("bad parameters when batch option is set");


        val params = eqlRun.getParams();
        if (params != null && seq < params.length)
            return params[seq];

        throw new EqlExecuteException("[" + eqlRun.getSqlId() + "] lack parameters at runtime");
    }

    private int computeOffset(int index) {
        int offset = 0;
        for (int i = 0; i <= index; ++i) {
            val ph = eqlRun.getPlaceHolders()[i];
            if (ph.getContextName() != null) {
                ++offset;
            }
        }
        return offset;
    }

    private Object findParamBySeq(int index) {
        val placeholder = eqlRun.getPlaceHolders()[index];
        if (placeholder.hasContextOnly()) {
            return EqlContext.get(placeholder.getContextName());
        }

        return getParamBySeq(placeholder, placeholder.getSeq() - 1);
    }
}
