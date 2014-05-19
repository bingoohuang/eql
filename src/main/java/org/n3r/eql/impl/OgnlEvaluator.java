package org.n3r.eql.impl;

import ognl.Ognl;
import org.n3r.eql.base.ExpressionEvaluator;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.EqlUtils;

import java.util.Map;

public class OgnlEvaluator implements ExpressionEvaluator {
    // private Logger log = LoggerFactory.getLogger(OgnlEvaluator.class);

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
        try {
            Map<String, Object> map = EqlUtils.mergeProperties(context, bean);
            return Ognl.getValue(expr, map);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
