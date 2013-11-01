package org.n3r.eql.parser;

public class IsEmptyPart implements EqlPart {
    protected final String expr;
    protected final MultiPart multiPart;

    public IsEmptyPart(String expr, MultiPart multiPart) {
        this.expr = expr;
        this.multiPart = multiPart;
    }

    @Override
    public String evalSql(Object bean) {
        return isEmpty(bean) ? multiPart.evalSql(bean) : "";
    }

    protected boolean isEmpty(Object bean) {
        Object target = SwitchPart.eval(bean, expr);
        return target == null || target.toString().length() == 0;
    }

    protected boolean isBlank(Object bean) {
        Object target = SwitchPart.eval(bean, expr);
        return target == null || target.toString().trim().length() == 0;
    }

    protected boolean isNull(Object bean) {
        Object target = SwitchPart.eval(bean, expr);
        return target == null;
    }

}
