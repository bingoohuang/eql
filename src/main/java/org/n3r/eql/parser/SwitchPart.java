package org.n3r.eql.parser;

import ognl.Ognl;
import ognl.OgnlException;

import java.util.List;
import java.util.Objects;

public class SwitchPart implements SqlPart {
    private final String condition;
    private final List<IfCondition> cases;

    public SwitchPart(String condition, List<IfCondition> cases) {
        this.condition = condition;
        this.cases = cases;
    }

    public List<IfCondition> getCases() {
        return cases;
    }

    @Override
    public String evalSql(Object bean) {
        Object target = eval(bean, condition);
        if (target == null) return "";
        String strTarget = target.toString();

        for (IfCondition ifCondition : cases) {
            if ("".equals(ifCondition.getExpr())
                    || Objects.equals(strTarget, ifCondition.getExpr()))
                return ifCondition.getValue().evalSql(bean);
        }

        return "";
    }

    public static Object eval(Object bean, String expr) {
        try {
            return Ognl.getValue(expr, bean);
        } catch (OgnlException e) {
            throw new RuntimeException("eval " + expr + " with " + bean + " failed", e);
        }
    }
}
