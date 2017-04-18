package org.n3r.eql.parser;

public class IsNotEmptyParser extends IsEmptyParser {
    public IsNotEmptyParser(String expr) {
        super(expr);
    }

    @Override
    public EqlPart createPart() {
        return new IsNotEmptyPart(expr, multiPart, elsePart);
    }
}
