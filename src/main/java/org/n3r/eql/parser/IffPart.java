package org.n3r.eql.parser;

import java.util.Map;

public class IffPart implements EqlPart {
    private final String expr;
    private final LiteralPart part;

    public IffPart(String expr, LiteralPart part) {
        this.expr = expr;
        this.part = part;
    }

    @Override
    public String evalSql(Object bean, Map<String, Object> executionContext) {
        boolean ok = IfPart.evalBool(bean, expr, executionContext);

        if (ok) return part.evalSql(bean, executionContext);

        return "";
    }

    public String getExpr() {
        return expr;
    }

    public LiteralPart getPart() {
        return part;
    }
}
