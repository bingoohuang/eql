package org.n3r.eql.parser;

public class IsNotNullPart extends IsEmptyPart {
    public IsNotNullPart(String expr, MultiPart multiPart) {
        super(expr, multiPart);
    }

    @Override
    public String evalSql(Object bean) {
        return !isNull(bean) ? multiPart.evalSql(bean) : "";
    }
}
