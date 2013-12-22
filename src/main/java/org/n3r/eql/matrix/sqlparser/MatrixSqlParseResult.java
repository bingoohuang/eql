package org.n3r.eql.matrix.sqlparser;

import org.n3r.eql.map.EqlRun;

public interface MatrixSqlParseResult {
    String getDatabaseName(EqlRun eqlRun);
}
