package org.n3r.eql.parser;

import org.n3r.eql.map.EqlRun;

public interface Sql {
    String evalSql(EqlRun eqlRun);
}
