package org.n3r.eql.parser;

import com.google.common.collect.Lists;
import ognl.Ognl;
import ognl.OgnlException;

import java.util.List;

public class IfPart implements SqlPart {
    private List<IfCondition> conditions = Lists.newArrayList();

    public IfPart(List<IfCondition> conditions) {
        this.conditions = conditions;
    }

    public List<IfCondition> getConditions() {
        return conditions;
    }

    @Override
    public String evalSql(Object bean) {
        for (IfCondition ifc : conditions) {
            boolean ok = evalBool(bean, ifc.getExpr());

            if (ok) return ifc.getValue().evalSql(bean);
        }

        return "";
    }

    public static boolean evalBool(Object bean, String expr) {
        try {
            Object value = Ognl.getValue(expr, bean);
            return value instanceof Boolean && ((Boolean) value).booleanValue();
        } catch (OgnlException e) {
            throw new RuntimeException("eval " + expr + " with " + bean + " failed", e);
        }
    }
}
