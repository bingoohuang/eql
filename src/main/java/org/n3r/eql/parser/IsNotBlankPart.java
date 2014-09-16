package org.n3r.eql.parser;

import org.n3r.eql.map.EqlRun;

public class IsNotBlankPart extends IsEmptyPart {
    public IsNotBlankPart(String expr, MultiPart multiPart, MultiPart elsePart) {
        super(expr, multiPart, elsePart);
    }

    @Override
    public String evalSql(EqlRun eqlRun) {
        return (!isBlank(eqlRun) ? multiPart : elsePart).evalSql(eqlRun);
    }
}
