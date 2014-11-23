package org.n3r.eql.codedesc;

import org.n3r.eql.spec.Spec;

public class CodeDesc {
    private final String columnName;
    private final Spec spec;

    public CodeDesc(String columnName, Spec spec) {
        this.columnName = columnName;
        this.spec = spec;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getDescLabel() {
        return spec.getName();
    }

    public String[] getParams() {
        return spec.getParams();
    }
}
