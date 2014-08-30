package org.n3r.eql.impl;

import org.n3r.eql.base.ExpressionEvaluator;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.Og;

public class OgnlEvaluator implements ExpressionEvaluator {
    @Override
    public Object eval(String expr, EqlRun eqlRun) {
        return Og.eval(expr, eqlRun.getMergedParamProperties());
    }


    @Override
    public Object evalDynamic(String expr, EqlRun eqlRun) {
        return Og.eval(expr, eqlRun.getMergedDynamicsProperties());
    }

    @Override
    public boolean evalBool(String expr, EqlRun eqlRun) {
        Object value = eval(expr, eqlRun);
        return value instanceof Boolean && ((Boolean) value).booleanValue();
    }

}
