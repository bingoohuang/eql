package org.n3r.eql.parser;

import org.n3r.eql.map.EqlRun;

public class IsNotBlankPart extends IsEmptyPart {
    public IsNotBlankPart(String expr, MultiPart multiPart) {
        super(expr, multiPart);
    }

    @Override
    public String evalSql(EqlRun eqlRun) {
        return !isBlank(eqlRun) ? multiPart.evalSql(eqlRun) : "";
    }
}
