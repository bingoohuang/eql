package org.n3r.eql.parser;

public class IsNotBlankPart extends IsEmptyPart {
    public IsNotBlankPart(String expr, MultiPart multiPart) {
        super(expr, multiPart);
    }

    @Override
    public String evalSql(Object bean) {
        return !isBlank(bean) ? multiPart.evalSql(bean) : "";
    }
}
