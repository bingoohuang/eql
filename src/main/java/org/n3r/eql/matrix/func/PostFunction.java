package org.n3r.eql.matrix.func;

import org.n3r.eql.matrix.MatrixTableFieldValue;

public class PostFunction extends PrefixFunction {
    @Override
    public String apply(MatrixTableFieldValue... fieldValues) {
        String value = find(fieldValues);

        return prefixSize < value.length() ? value.substring(value.length() - prefixSize) : value;
    }
}
