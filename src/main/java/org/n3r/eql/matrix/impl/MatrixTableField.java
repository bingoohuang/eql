package org.n3r.eql.matrix.impl;

import lombok.AllArgsConstructor;
import org.n3r.eql.matrix.MatrixTableFieldValue;

@AllArgsConstructor
public class MatrixTableField {
    public String tableName;
    public String fieldName;

    public boolean find(MatrixTableFieldValue... fieldValues) {
        for (MatrixTableFieldValue fieldValue : fieldValues) {
            if (fieldValue.tableName.equals(tableName) && fieldName.equals(fieldValue.fieldName)) {
                return true;
            }
        }

        return false;
    }
}
