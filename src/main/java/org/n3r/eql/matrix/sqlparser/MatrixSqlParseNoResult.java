package org.n3r.eql.matrix.sqlparser;

import org.n3r.eql.map.EqlRun;

public class MatrixSqlParseNoResult implements MatrixSqlParseResult{
    public static MatrixSqlParseNoResult instance = new MatrixSqlParseNoResult();

    @Override
    public String getDatabaseName(EqlRun eqlRun) {
        return null;
    }
}
