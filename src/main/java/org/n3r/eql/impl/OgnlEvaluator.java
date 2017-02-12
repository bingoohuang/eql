package org.n3r.eql.impl;

import com.google.common.collect.Maps;
import lombok.val;
import org.n3r.eql.base.ExpressionEvaluator;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.Og;

import java.util.ArrayList;
import java.util.Map;

public class OgnlEvaluator implements ExpressionEvaluator {
    @Override
    public Object eval(String expr, EqlRun eqlRun) {
        if (!eqlRun.isIterateOption()) {
            return Og.eval(expr, eqlRun.getMergedParamProperties(), eqlRun.getCachedProperties());
        }

        ArrayList<Object> result = new ArrayList<Object>();
        Object iteratableOrArray = eqlRun.getIterateParams();
        if (iteratableOrArray == null) return result;

        if (iteratableOrArray instanceof Iterable) {
            for (Object element : (Iterable<Object>) iteratableOrArray) {
                val params = eqlRun.getMergedParamPropertiesWith(element);
                val cached = Maps.<Object, Map<String, Object>>newHashMap();
                Object eval = Og.eval(expr, params, cached);
                result.add(eval);
            }
            return result;
        }

        if (iteratableOrArray.getClass().isArray()) {
            for (Object element : (Object[]) iteratableOrArray) {
                val params = eqlRun.getMergedParamPropertiesWith(element);
                val cached = Maps.<Object, Map<String, Object>>newHashMap();
                Object eval = Og.eval(expr, params, cached);
                result.add(eval);
            }
            return result;
        }

        return result;
    }

    @Override
    public Object evalDynamic(String expr, EqlRun eqlRun) {
        val params = eqlRun.getMergedDynamicsProperties();
        return Og.eval(expr, params, eqlRun.getCachedProperties());
    }

    @Override
    public boolean evalBool(String expr, EqlRun eqlRun) {
        Object value = eval(expr, eqlRun);
        return value instanceof Boolean && ((Boolean) value).booleanValue();
    }
}
