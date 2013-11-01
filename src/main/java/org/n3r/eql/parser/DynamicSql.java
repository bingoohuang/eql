package org.n3r.eql.parser;

import java.util.Map;

public class DynamicSql implements Sql {
    private MultiPart parts = new MultiPart();

    public DynamicSql(MultiPart parts) {
        this.parts = parts;
    }

    public MultiPart getParts() {
        return parts;
    }

    @Override
    public String evalSql(Object bean, Map<String, Object> executionContext) {
        return parts.evalSql(bean, executionContext);
    }
}
