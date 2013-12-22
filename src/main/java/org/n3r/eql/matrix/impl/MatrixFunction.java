package org.n3r.eql.matrix.impl;

import org.n3r.eql.matrix.MatrixTableFieldValue;

public interface MatrixFunction {
    void configRelativeTableFields(MatrixTableField... matrixTableFields);

    void configFunctionParameters(String... realFuncParams);

    boolean match(MatrixTableFieldValue[] fieldValues);

    String apply(MatrixTableFieldValue... fieldValues);

    boolean relativeTo(String tableName);

    boolean relativeTo(String tableName, String columnName);
}
