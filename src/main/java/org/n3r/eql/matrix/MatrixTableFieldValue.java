package org.n3r.eql.matrix;

import org.n3r.eql.matrix.impl.MatrixTableField;

public class MatrixTableFieldValue extends MatrixTableField {
    public String fieldValue;

    public MatrixTableFieldValue(String tableName, String fieldName, String fieldValue) {
        super(tableName, fieldName);
        this.fieldValue = fieldValue;
    }

    public MatrixTableFieldValue(MatrixTableFieldValue other) {
        this(other.tableName, other.fieldName, other.fieldValue);
    }
}
