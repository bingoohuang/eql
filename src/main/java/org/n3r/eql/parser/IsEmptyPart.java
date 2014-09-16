package org.n3r.eql.parser;

import org.n3r.eql.base.ExpressionEvaluator;
import org.n3r.eql.map.EqlRun;

public class IsEmptyPart implements EqlPart {
    protected final String expr;
    protected final MultiPart multiPart;
    protected final MultiPart elsePart;

    public IsEmptyPart(String expr, MultiPart multiPart, MultiPart elsePart) {
        this.expr = expr;
        this.multiPart = multiPart;
        this.elsePart = elsePart;
    }

    @Override
    public String evalSql(EqlRun eqlRun) {
        return (isEmpty(eqlRun) ? multiPart : elsePart).evalSql(eqlRun);
    }

    protected boolean isEmpty(EqlRun eqlRun) {
        ExpressionEvaluator evaluator = eqlRun.getEqlConfig().getExpressionEvaluator();
        Object target = evaluator.eval(expr, eqlRun);
        return target == null || target.toString().length() == 0;
    }

    protected boolean isBlank(EqlRun eqlRun) {
        ExpressionEvaluator evaluator = eqlRun.getEqlConfig().getExpressionEvaluator();
        Object target = evaluator.eval(expr, eqlRun);
        return target == null || target.toString().trim().length() == 0;
    }

    protected boolean isNull(EqlRun eqlRun) {
        ExpressionEvaluator evaluator = eqlRun.getEqlConfig().getExpressionEvaluator();
        Object target = evaluator.eval(expr, eqlRun);
        return target == null;
    }

}
