package org.n3r.eql.matrix.func;

import org.n3r.eql.matrix.MatrixTableFieldValue;

public class MiddleFunction extends SingleFieldBaseFunction {
    int start;
    int len;

    @Override
    public void configFunctionParameters(String... realFuncParams) {
        if (realFuncParams.length != 2) {
            throw new RuntimeException("mid function need start and size parameters");
        }

        start = Integer.parseInt(realFuncParams[0]);
        if (start < 0) {
            throw new RuntimeException("mid function's start should non-negative");
        }

        len = Integer.parseInt(realFuncParams[1]);
        if (len <= 0) {
            throw new RuntimeException("mid function's len should be positive");
        }
    }

    @Override
    public String apply(MatrixTableFieldValue... fieldValues) {
        String value = find(fieldValues);

        return mid(value, start, len);
    }

    private String mid(String value, int start, int len) {
        if (start > value.length()) throw new RuntimeException("start " + start + " is larger than " + value);
        if (start + len > value.length())
            throw new RuntimeException("sum of start [" + start + "] and len [" + len + "] is larger than size of value [" + value + "]");

        return value.substring(start, start + len);
    }
}
