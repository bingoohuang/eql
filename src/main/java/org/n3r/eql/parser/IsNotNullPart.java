package org.n3r.eql.parser;

import org.n3r.eql.map.EqlRun;

public class IsNotNullPart extends IsEmptyPart {
    public IsNotNullPart(String expr, MultiPart multiPart) {
        super(expr, multiPart);
    }

    @Override
    public String evalSql(EqlRun eqlRun) {
        return !isNull(eqlRun) ? multiPart.evalSql(eqlRun) : "";
    }
}
