package org.n3r.eql.parser;

import org.n3r.eql.map.EqlRun;

public class IsBlankPart extends IsEmptyPart {
    public IsBlankPart(String expr, MultiPart multiPart) {
        super(expr, multiPart);
    }

    @Override
    public String evalSql(EqlRun eqlRun) {
        return isBlank(eqlRun) ? multiPart.evalSql(eqlRun) : "";
    }
}
