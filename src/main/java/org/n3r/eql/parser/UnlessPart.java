package org.n3r.eql.parser;

import lombok.Value;
import org.n3r.eql.base.ExpressionEvaluator;
import org.n3r.eql.map.EqlRun;

@Value
public class UnlessPart implements EqlPart {
    private final String expr;
    private final MultiPart part;

    @Override
    public String evalSql(EqlRun eqlRun) {
        ExpressionEvaluator evaluator = eqlRun.getEqlConfig().getExpressionEvaluator();
        return !evaluator.evalBool(expr, eqlRun) ? part.evalSql(eqlRun) : "";
    }
}
