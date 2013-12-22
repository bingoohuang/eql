package org.n3r.eql.matrix.func;

import org.n3r.eql.matrix.MatrixTableFieldValue;
import org.n3r.eql.matrix.func.SingleFieldBaseFunction;

public class ModFunction extends SingleFieldBaseFunction {
    private int modValue;


    @Override
    public String apply(MatrixTableFieldValue... fieldValues) {
        String value = find(fieldValues);

        return "" + (Long.parseLong(value) % modValue);
    }

    @Override
    public void configFunctionParameters(String... realFuncParams) {
        if (realFuncParams.length != 1) {
            throw new RuntimeException("mod function need only size param");
        }

        modValue = Integer.parseInt(realFuncParams[0]);
    }


}
