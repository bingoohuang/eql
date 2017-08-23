package org.n3r.eql.parser;

import org.n3r.eql.base.ExpressionEvaluator;
import org.n3r.eql.map.EqlRun;

import java.util.Collection;
import java.util.Map;

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

        return isEmpty(target) || target.toString().length() == 0;
    }

    protected boolean isBlank(EqlRun eqlRun) {
        ExpressionEvaluator evaluator = eqlRun.getEqlConfig().getExpressionEvaluator();
        Object target = evaluator.eval(expr, eqlRun);

        return isEmpty(target) || target.toString().trim().length() == 0;
    }

    public static boolean isEmpty(Object target) {
        if (target == null) return true;
        if (target instanceof Collection) return ((Collection) target).isEmpty();
        if (target instanceof Map) return ((Map) target).isEmpty();
        if (target instanceof Iterable) return !((Iterable) target).iterator().hasNext();
        if (target instanceof CharSequence) return ((CharSequence) target).length() == 0;
        if (target.getClass().isArray()) return ((Object[]) target).length == 0;

        return false;
    }

    protected boolean isNull(EqlRun eqlRun) {
        ExpressionEvaluator evaluator = eqlRun.getEqlConfig().getExpressionEvaluator();
        Object target = evaluator.eval(expr, eqlRun);
        return target == null;
    }
}
