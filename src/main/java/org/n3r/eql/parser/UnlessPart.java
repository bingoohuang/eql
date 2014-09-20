package org.n3r.eql.parser;

import org.n3r.eql.base.ExpressionEvaluator;
import org.n3r.eql.map.EqlRun;

public class UnlessPart implements EqlPart {
    private final String expr;
    private final MultiPart part;

    public UnlessPart(String expr, MultiPart part) {
        this.expr = expr;
        this.part = part;
    }

    @Override
    public String evalSql(EqlRun eqlRun) {
        ExpressionEvaluator evaluator = eqlRun.getEqlConfig().getExpressionEvaluator();
        return !evaluator.evalBool(expr, eqlRun) ? part.evalSql(eqlRun) : "";
    }

    public String getExpr() {
        return expr;
    }

    public MultiPart getPart() {
        return part;
    }

}
