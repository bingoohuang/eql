package org.n3r.eql.parser;

import com.google.common.base.Objects;
import org.n3r.eql.base.ExpressionEvaluator;
import org.n3r.eql.map.EqlRun;

import java.util.List;

public class SwitchPart implements EqlPart {
    private final String condition;
    private final List<IfCondition> cases;

    public SwitchPart(String condition, List<IfCondition> cases) {
        this.condition = condition;
        this.cases = cases;
    }

    public List<IfCondition> getCases() {
        return cases;
    }

    @Override
    public String evalSql(EqlRun eqlRun) {
        ExpressionEvaluator evaluator = eqlRun.getEqlConfig().getExpressionEvaluator();
        Object target = evaluator.eval(condition, eqlRun);
        if (target == null) return "";
        String strTarget = target.toString();

        for (IfCondition ifCondition : cases) {
            if ("".equals(ifCondition.getExpr())
                    || Objects.equal(strTarget, ifCondition.getExpr()))
                return ifCondition.getValue().evalSql(eqlRun);
        }

        return "";
    }

}
