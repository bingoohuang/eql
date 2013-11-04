package org.n3r.eql.parser;

import org.n3r.eql.map.EqlRun;

public interface EqlPart {

    String evalSql(EqlRun eqlRun);
}
