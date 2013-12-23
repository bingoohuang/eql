package org.n3r.eql.matrix.func;


import org.n3r.eql.matrix.MatrixTableFieldValue;

public class PrefixFunction extends SingleFieldBaseFunction {
    protected int prefixSize;


    @Override
    public String apply(MatrixTableFieldValue... fieldValues) {
        String value = find(fieldValues);

        return prefixSize < value.length() ? value.substring(0, prefixSize) : value;
    }

    @Override
    public void configFunctionParameters(String... realFuncParams) {
        if (realFuncParams.length != 1) {
            throw new RuntimeException("prefix/postfix function need only size param");
        }

        prefixSize = Integer.parseInt(realFuncParams[0]);
    }

}
