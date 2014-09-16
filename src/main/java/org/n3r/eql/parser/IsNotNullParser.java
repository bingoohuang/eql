package org.n3r.eql.parser;

public class IsNotNullParser extends IsEmptyParser {
    public IsNotNullParser(String expr) {
        super(expr);
    }

    @Override
    public EqlPart createPart() {
        return new IsNotNullPart(expr, multiPart, elsePart);
    }
}
