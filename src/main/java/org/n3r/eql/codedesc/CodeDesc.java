package org.n3r.eql.codedesc;

import lombok.Value;
import org.n3r.eql.spec.Spec;

@Value
public class CodeDesc {
    private final String columnName;
    private final Spec spec;

    public String getDescLabel() {
        return spec.getName();
    }

    public String[] getParams() {
        return spec.getParams();
    }
}
