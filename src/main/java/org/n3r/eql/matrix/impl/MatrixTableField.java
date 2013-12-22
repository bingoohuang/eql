package org.n3r.eql.matrix.impl;

import org.n3r.eql.matrix.MatrixTableFieldValue;

public class MatrixTableField {
    public String tableName;
    public String fieldName;

    public MatrixTableField(String tableName, String fieldName) {
        this.tableName = tableName;
        this.fieldName = fieldName;
    }

    public boolean find(MatrixTableFieldValue... fieldValues) {
        for (MatrixTableFieldValue fieldValue : fieldValues) {
            if (fieldValue.tableName.equals(tableName) && fieldName.equals(fieldValue.fieldName)) {
                return true;
            }
        }

        return false;
    }
}
