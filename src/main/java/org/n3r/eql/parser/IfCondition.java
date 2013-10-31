package org.n3r.eql.parser;

public class IfCondition {
    private String expr;
    private MultiPart value;

    public IfCondition(String expr, MultiPart value) {
        this.expr = ParserUtils.trim(expr);
        this.value = value;
    }

    public String getExpr() {
        return expr;
    }

    public MultiPart getValue() {
        return value;
    }
}
