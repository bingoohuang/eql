package org.n3r.eql.parser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.n3r.eql.map.EqlRun;

@AllArgsConstructor
public class DynamicSql implements Sql {
    @Getter private MultiPart parts = new MultiPart();

    @Override
    public String evalSql(EqlRun eqlRun) {
        return parts.evalSql(eqlRun);
    }
}
