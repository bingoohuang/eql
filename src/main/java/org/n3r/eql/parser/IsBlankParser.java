package org.n3r.eql.parser;

public class IsBlankParser extends IsEmptyParser {
    public IsBlankParser(String expr) {
        super(expr);
    }

    @Override
    public EqlPart createPart() {
        return new IsBlankPart(expr, multiPart, elsePart);
    }
}
