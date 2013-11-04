package org.n3r.eql.parser;

import org.n3r.eql.base.ExpressionEvaluator;
import org.n3r.eql.map.EqlRun;

public class IsEmptyPart implements EqlPart {
    protected final String expr;
    protected final MultiPart multiPart;

    public IsEmptyPart(String expr, MultiPart multiPart) {
        this.expr = expr;
        this.multiPart = multiPart;
    }

    @Override
    public String evalSql(EqlRun eqlRun) {
        return isEmpty(eqlRun) ? multiPart.evalSql(eqlRun) : "";
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
