package org.n3r.eql.parser;

import lombok.Value;
import lombok.val;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.EqlUtils;

@Value
public class InPart implements EqlPart {
    private final String inParamsContainer;

    @Override
    public String evalSql(EqlRun eqlRun) {
        val items = EqlUtils.evalCollection(inParamsContainer, eqlRun);
        if (items == null) return "null";

        val iterator = items.iterator();
        val questions = new StringBuilder();

        for (int i = 0; iterator.hasNext(); ++i, iterator.next()) {
            if (i > 0) questions.append(',');

            questions.append('#').append(inParamsContainer)
                    .append('[').append(i).append(']').append('#');
        }

        val eval = questions.toString();
        return eval.isEmpty() ? "null" : eval;
    }
}
