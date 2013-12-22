package org.n3r.eql.matrix.func;


import org.n3r.eql.matrix.MatrixTableFieldValue;

public class ValFunction extends SingleFieldBaseFunction {
    @Override
    public String apply(MatrixTableFieldValue... fieldValues) {
        return find(fieldValues);
    }

    @Override
    public void configFunctionParameters(String... realFuncParams) {
        if (realFuncParams.length != 0) {
            throw new RuntimeException("val function need no param");
        }
    }

}