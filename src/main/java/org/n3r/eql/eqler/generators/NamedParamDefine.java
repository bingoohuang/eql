package org.n3r.eql.eqler.generators;

import org.n3r.eql.eqler.annotations.NamedParam;

public class NamedParamDefine {
    private final int index;
    private final NamedParam namedParam;
    private final Class<?> paramType;

    public NamedParamDefine(int index, Class<?> paramType, NamedParam namedParam) {
        this.index = index;
        this.namedParam = namedParam;
        this.paramType = paramType;
    }

    public String getParamName() {
        return namedParam.value();
    }

    public Class<?> getParamType() {
        return paramType;
    }
}
