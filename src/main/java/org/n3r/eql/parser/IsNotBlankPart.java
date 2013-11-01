package org.n3r.eql.parser;

import java.util.Map;

public class IsNotBlankPart extends IsEmptyPart {
    public IsNotBlankPart(String expr, MultiPart multiPart) {
        super(expr, multiPart);
    }

    @Override
    public String evalSql(Object bean, Map<String, Object> executionContext) {
        return !isBlank(bean, executionContext) ? multiPart.evalSql(bean, executionContext) : "";
    }
}
