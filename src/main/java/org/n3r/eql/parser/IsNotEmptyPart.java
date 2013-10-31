package org.n3r.eql.parser;

public class IsNotEmptyPart extends IsEmptyPart {
    public IsNotEmptyPart(String expr, MultiPart multiPart) {
        super(expr, multiPart);
    }

    @Override
    public String evalSql(Object bean) {
        return !isEmpty(bean) ? multiPart.evalSql(bean) : "";
    }
}
