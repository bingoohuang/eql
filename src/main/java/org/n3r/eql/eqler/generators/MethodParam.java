package org.n3r.eql.eqler.generators;

public class MethodParam {

    private final int paramIndex;
    private final Class<?> paramType;
    private final EqlPageHandler eqlPageHandler;

    public MethodParam(int paramIndex, Class<?> paramType, EqlPageHandler eqlPageHandler) {
        this.paramIndex = paramIndex;
        this.paramType = paramType;
        this.eqlPageHandler = eqlPageHandler;
    }

    public Class<?> getParamType() {
        return paramType;
    }

    public int getParamIndex() {
        return paramIndex;
    }


    public boolean isNormal() {
        return eqlPageHandler == null;
    }
}
