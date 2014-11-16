package org.n3r.eql.impl;

import org.n3r.eql.base.ExpressionEvaluator;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.Og;

import java.util.ArrayList;
import java.util.Collection;

public class OgnlEvaluator implements ExpressionEvaluator {
    @Override
    public Object eval(String expr, EqlRun eqlRun) {
        if (eqlRun.hasBatchOption()) {
            Collection<Object> collection = eqlRun.getBatchCollectionParams();
            int size = collection.size();
            ArrayList<Object> result = new ArrayList<Object>(size);
            for (Object element : collection) {
                Object eval = Og.eval(expr, eqlRun.getMergedParamPropertiesWith(element));
                result.add(eval);
            }

            return result;
        } else {
            return Og.eval(expr, eqlRun.getMergedParamProperties());
        }
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
