package org.n3r.eql.parser;

import org.n3r.eql.map.EqlRun;

public class IsBlankPart extends IsEmptyPart {
    public IsBlankPart(String expr, MultiPart multiPart, MultiPart elsePart) {
        super(expr, multiPart, elsePart);
    }

    @Override
    public String evalSql(EqlRun eqlRun) {
        return (isBlank(eqlRun) ? multiPart : elsePart).evalSql(eqlRun);
    }
}
