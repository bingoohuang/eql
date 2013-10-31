package org.n3r.eql.parser;

public class DynamicSql implements Sql {
    private MultiPart parts = new MultiPart();

    public DynamicSql(MultiPart parts) {
        this.parts = parts;
    }

    public MultiPart getParts() {
        return parts;
    }

    @Override
    public String evalSql(Object bean) {
        return parts.evalSql(bean);
    }
}
