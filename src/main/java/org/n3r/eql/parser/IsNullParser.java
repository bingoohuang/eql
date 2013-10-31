package org.n3r.eql.parser;

public class IsNullParser extends IsEmptyParser {
    public IsNullParser(String expr) {
        super(expr);
    }

    @Override
    public SqlPart createPart() {
        return new IsNullPart(expr, multiPart);
    }

}
