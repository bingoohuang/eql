package org.n3r.eql.matrix.sqlparser;

import org.n3r.eql.matrix.MatrixTableFieldValue;

public class SqlFieldIndex extends MatrixTableFieldValue {
    public final int variantIndex;

    public SqlFieldIndex(String tableName, String fieldName, int variantIndex) {
        super(tableName, fieldName, null);
        this.variantIndex = variantIndex;
    }
}
