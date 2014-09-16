package org.n3r.eql.parser;

import org.n3r.eql.map.EqlRun;

public class IsNotNullPart extends IsEmptyPart {
    public IsNotNullPart(String expr, MultiPart multiPart, MultiPart elsePart) {
        super(expr, multiPart, elsePart);
    }

    @Override
    public String evalSql(EqlRun eqlRun) {
        return (!isNull(eqlRun) ? multiPart : elsePart).evalSql(eqlRun);
    }
}
