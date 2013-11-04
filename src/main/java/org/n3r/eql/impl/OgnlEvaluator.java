package org.n3r.eql.impl;

import ognl.NoSuchPropertyException;
import ognl.Ognl;
import ognl.OgnlException;
import org.n3r.eql.base.ExpressionEvaluator;
import org.n3r.eql.map.EqlRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class OgnlEvaluator implements ExpressionEvaluator {
    private Logger log = LoggerFactory.getLogger(OgnlEvaluator.class);


    @Override
    public Object eval(String expr, EqlRun eqlRun) {
        return eval(expr, eqlRun.getExecutionContext(), eqlRun.getParamBean());
    }


    @Override
    public Object evalDynamic(String expr, EqlRun eqlRun) {
        return eval(expr, eqlRun.getExecutionContext(), eqlRun.getDynamicsBean());
    }

    @Override
    public boolean evalBool(String expr, EqlRun eqlRun) {
        Object value = eval(expr, eqlRun);
        return value instanceof Boolean && ((Boolean) value).booleanValue();
    }

    private Object eval(String expr, Map<String, Object> context, Object bean) {
        Exception ex = null;
        try {
            return Ognl.getValue(expr, bean);
        } catch (NoSuchPropertyException e) {
            // will try again from context
        } catch (Exception e) {
            ex = e;
        }

        try {
            if (ex == null) return Ognl.getValue(expr, context);
        } catch (OgnlException e) {
            ex = e;
        }

        log.error("eval {} with {} and context {} error", expr, bean, context, ex);
        return null;
    }
}
