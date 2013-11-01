package org.n3r.eql.parser;

public class DynamicEql implements Eql {
    private MultiPart parts = new MultiPart();

    public DynamicEql(MultiPart parts) {
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
