package org.n3r.eql.parser;

public class IsNullParser extends IsEmptyParser {
    public IsNullParser(String expr) {
        super(expr);
    }

    @Override
    public EqlPart createPart() {
        return new IsNullPart(expr, multiPart);
    }

}
