package org.n3r.eql.parser;

import com.google.common.collect.Lists;
import ognl.Ognl;
import ognl.OgnlException;

import java.util.List;
import java.util.Map;

public class IfPart implements EqlPart {
    private List<IfCondition> conditions = Lists.newArrayList();

    public IfPart(List<IfCondition> conditions) {
        this.conditions = conditions;
    }

    public List<IfCondition> getConditions() {
        return conditions;
    }

    @Override
    public String evalSql(Object bean, Map<String, Object> executionContext) {
        for (IfCondition ifc : conditions) {
            boolean ok = evalBool(bean, ifc.getExpr(), executionContext);

            if (ok) return ifc.getValue().evalSql(bean, executionContext);
        }

        return "";
    }

    public static boolean evalBool(Object bean, String expr, Map<String, Object> executionContext) {
        try {
            Object value = Ognl.getValue(expr, executionContext, bean);
            return value instanceof Boolean && ((Boolean) value).booleanValue();
        } catch (OgnlException e) {
            throw new RuntimeException("eval " + expr + " with " + bean + " failed", e);
        }
    }
}
