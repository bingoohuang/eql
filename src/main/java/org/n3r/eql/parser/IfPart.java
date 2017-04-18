package org.n3r.eql.parser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.n3r.eql.map.EqlRun;

import java.util.List;

@AllArgsConstructor
public class IfPart implements EqlPart {
    @Getter private final List<IfCondition> conditions;

    @Override
    public String evalSql(EqlRun eqlRun) {
        val evaluator = eqlRun.getEqlConfig().getExpressionEvaluator();
        for (IfCondition ifc : conditions) {
            boolean ok = evaluator.evalBool(ifc.getExpr(), eqlRun);

            if (ok) return ifc.getValue().evalSql(eqlRun);
        }

        return "";
    }
}
