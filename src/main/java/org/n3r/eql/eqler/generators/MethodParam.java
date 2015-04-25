package org.n3r.eql.eqler.generators;

public class MethodParam {

    private final int paramIndex;
    private final Class<?> paramType;
    private final boolean normal;

    public MethodParam(int paramIndex, Class<?> paramType, boolean normal) {
        this.paramIndex = paramIndex;
        this.paramType = paramType;
        this.normal = normal;
    }

    public Class<?> getParamType() {
        return paramType;
    }

    public int getParamIndex() {
        return paramIndex;
    }


    public boolean isNormal() {
        return normal;
    }
}
