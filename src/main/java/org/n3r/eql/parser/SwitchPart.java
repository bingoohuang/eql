package org.n3r.eql.parser;

import com.google.common.base.Objects;
import ognl.NoSuchPropertyException;
import ognl.Ognl;

import java.util.List;
import java.util.Map;

public class SwitchPart implements EqlPart {
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
    public String evalSql(Object bean, Map<String, Object> executionContext) {
        Object target = eval(bean, condition, executionContext);
        if (target == null) return "";
        String strTarget = target.toString();

        for (IfCondition ifCondition : cases) {
            if ("".equals(ifCondition.getExpr())
                    || Objects.equal(strTarget, ifCondition.getExpr()))
                return ifCondition.getValue().evalSql(bean, executionContext);
        }

        return "";
    }

    public static Object eval(Object bean, String expr, Map<String, Object> executionContext) {
        try {
            return Ognl.getValue(expr, executionContext, bean);
        } catch (NoSuchPropertyException ex) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("eval " + expr + " with " + bean + " failed", e);
        }
    }
}
