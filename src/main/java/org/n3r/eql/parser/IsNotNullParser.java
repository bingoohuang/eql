package org.n3r.eql.parser;

public class IsNotNullParser extends IsEmptyParser {
    public IsNotNullParser(String expr) {
        super(expr);
    }

    @Override
    public SqlPart createPart() {
        return new IsNotNullPart(expr, multiPart);
    }
}
