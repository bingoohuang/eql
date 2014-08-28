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
        return eval(expr, eqlRun.getMergedParamProperties());
    }


    @Override
    public Object evalDynamic(String expr, EqlRun eqlRun) {
        return eval(expr, eqlRun.getMergedDynamicsProperties());
    }

    @Override
    public boolean evalBool(String expr, EqlRun eqlRun) {
        Object value = eval(expr, eqlRun);
        return value instanceof Boolean && ((Boolean) value).booleanValue();
    }

    private Object eval(String expr, Map<String, Object> mergeProperties) {
        Exception ex = null;
        try {
            return Ognl.getValue(expr, mergeProperties);
        } catch (NoSuchPropertyException e) { // ignore
        } catch (OgnlException e) {
            if (e.getMessage().indexOf("source is null for getProperty") < 0) ex = e;
        } catch (Exception e) {
            ex = e;
        }

        if (ex != null) log.warn("error while eval " + expr, ex);
        return null;
    }
}
