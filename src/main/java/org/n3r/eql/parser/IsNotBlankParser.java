package org.n3r.eql.parser;

public class IsNotBlankParser extends IsEmptyParser {
    public IsNotBlankParser(String expr) {
        super(expr);
    }

    @Override
    public EqlPart createPart() {
        return new IsNotBlankPart(expr, multiPart);
    }
}
