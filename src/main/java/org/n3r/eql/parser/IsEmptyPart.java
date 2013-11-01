package org.n3r.eql.parser;

import java.util.Map;

public class IsEmptyPart implements EqlPart {
    protected final String expr;
    protected final MultiPart multiPart;

    public IsEmptyPart(String expr, MultiPart multiPart) {
        this.expr = expr;
        this.multiPart = multiPart;
    }

    @Override
    public String evalSql(Object bean, Map<String, Object> executionContext) {
        return isEmpty(bean, executionContext) ? multiPart.evalSql(bean, executionContext) : "";
    }

    protected boolean isEmpty(Object bean, Map<String, Object> executionContext) {
        Object target = SwitchPart.eval(bean, expr, executionContext);
        return target == null || target.toString().length() == 0;
    }

    protected boolean isBlank(Object bean, Map<String, Object> executionContext) {
        Object target = SwitchPart.eval(bean, expr, executionContext);
        return target == null || target.toString().trim().length() == 0;
    }

    protected boolean isNull(Object bean, Map<String, Object> executionContext) {
        Object target = SwitchPart.eval(bean, expr, executionContext);
        return target == null;
    }

}
