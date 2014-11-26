package org.n3r.eql.matrix.impl;

import org.n3r.eql.matrix.MatrixTableFieldValue;
import org.n3r.eql.matrix.RealPartition;

public class MatrixRule {
    public int ruleNo;
    public MatrixFunction function;
    public MatrixMapper mapper;


    public RealPartition go(MatrixTableFieldValue... fieldValues) {
        if (!function.match(fieldValues)) return null;

        String value = function.apply(fieldValues);

        return mapper.map(value);
    }

    public boolean relativeTo(String tableName) {
        return function.relativeTo(tableName);
    }

    public boolean relativeTo(String tableName, String columnName) {
        return function.relativeTo(tableName, columnName);
    }
}
