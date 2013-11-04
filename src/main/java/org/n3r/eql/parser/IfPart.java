package org.n3r.eql.parser;

import com.google.common.collect.Lists;
import org.n3r.eql.base.ExpressionEvaluator;
import org.n3r.eql.map.EqlRun;

import java.util.List;

public class IfPart implements EqlPart {
    private List<IfCondition> conditions = Lists.newArrayList();

    public IfPart(List<IfCondition> conditions) {
        this.conditions = conditions;
    }

    public List<IfCondition> getConditions() {
        return conditions;
    }

    @Override
    public String evalSql(EqlRun eqlRun) {
        ExpressionEvaluator evaluator = eqlRun.getEqlConfig().getExpressionEvaluator();
        for (IfCondition ifc : conditions) {
            boolean ok = evaluator.evalBool(ifc.getExpr(), eqlRun);

            if (ok) return ifc.getValue().evalSql(eqlRun);
        }

        return "";
    }


}
