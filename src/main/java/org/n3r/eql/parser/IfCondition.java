package org.n3r.eql.parser;

import lombok.Value;

@Value
public class IfCondition {
    private String expr;
    private MultiPart value;

    public IfCondition(String expr, MultiPart value) {
        this.expr = ParserUtils.trim(expr);
        this.value = value;
    }
}
