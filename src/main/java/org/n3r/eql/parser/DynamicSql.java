package org.n3r.eql.parser;

import org.n3r.eql.map.EqlRun;

public class DynamicSql implements Sql {
    private MultiPart parts = new MultiPart();

    public DynamicSql(MultiPart parts) {
        this.parts = parts;
    }

    public MultiPart getParts() {
        return parts;
    }

    @Override
    public String evalSql(EqlRun eqlRun) {
        return parts.evalSql(eqlRun);
    }
}
