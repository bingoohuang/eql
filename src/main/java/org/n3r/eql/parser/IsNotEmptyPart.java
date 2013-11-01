package org.n3r.eql.parser;

import java.util.Map;

public class IsNotEmptyPart extends IsEmptyPart {
    public IsNotEmptyPart(String expr, MultiPart multiPart) {
        super(expr, multiPart);
    }

    @Override
    public String evalSql(Object bean, Map<String, Object> executionContext) {
        return !isEmpty(bean, executionContext) ? multiPart.evalSql(bean, executionContext) : "";
    }
}
