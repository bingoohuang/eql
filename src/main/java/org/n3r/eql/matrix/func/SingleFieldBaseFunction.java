package org.n3r.eql.matrix.func;

import org.n3r.eql.matrix.MatrixTableFieldValue;
import org.n3r.eql.matrix.impl.MatrixFunction;
import org.n3r.eql.matrix.impl.MatrixTableField;

public abstract class SingleFieldBaseFunction implements MatrixFunction {
    private MatrixTableField matrixTableField;

    @Override
    public void configRelativeTableFields(MatrixTableField... matrixTableFields) {
        if (matrixTableFields.length != 1) {
            throw new RuntimeException("function need only one relative table field");
        }
        this.matrixTableField = matrixTableFields[0];
    }

    @Override
    public  boolean match(MatrixTableFieldValue[] fieldValues) {
        return matrixTableField.find(fieldValues);
    }

    protected String find(MatrixTableFieldValue... fieldValues) {
        for (MatrixTableFieldValue fieldValue : fieldValues) {
            if (!fieldValue.tableName.equals(matrixTableField.tableName)) continue;
            if (!fieldValue.fieldName.equals(matrixTableField.fieldName)) continue;

            return fieldValue.fieldValue;
        }

        return null;
    }

    @Override
    public boolean relativeTo(String tableName) {
        return matrixTableField.tableName.equalsIgnoreCase(tableName);
    }

    @Override
    public boolean relativeTo(String tableName, String columnName) {
        return relativeTo(tableName) && matrixTableField.fieldName.equalsIgnoreCase(columnName);
    }
}
