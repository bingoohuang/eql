package org.n3r.eql.parser;

import org.n3r.eql.map.EqlRun;

public class IsNotEmptyPart extends IsEmptyPart {
    public IsNotEmptyPart(String expr, MultiPart multiPart) {
        super(expr, multiPart);
    }

    @Override
    public String evalSql(EqlRun eqlRun) {
        return !isEmpty(eqlRun) ? multiPart.evalSql(eqlRun) : "";
    }
}
