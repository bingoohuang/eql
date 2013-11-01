package org.n3r.eql.parser;

import java.util.Map;

public class IsBlankPart extends IsEmptyPart {
    public IsBlankPart(String expr, MultiPart multiPart) {
        super(expr, multiPart);
    }

    @Override
    public String evalSql(Object bean, Map<String, Object> executionContext) {
        return isBlank(bean, executionContext) ? multiPart.evalSql(bean, executionContext) : "";
    }
}
