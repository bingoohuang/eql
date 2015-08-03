package org.n3r.eql.eqler.generators;

import org.n3r.eql.eqler.annotations.Dynamic;
import org.n3r.eql.eqler.annotations.Param;
import org.n3r.eql.eqler.annotations.ReturnType;
import org.n3r.eql.eqler.annotations.SqlId;

import java.lang.annotation.Annotation;

public class MethodParam {
    private int paramIndex;
    private Class<?> paramType;
    private Param param;
    private Dynamic dynamic;
    private SqlId sqlId;
    private ReturnType returnType;
    private int seqParamIndex = -1;
    private int seqDynamicIndex = -1;
    private Annotation[] paramAnnotations;
    private int offset;

    public Annotation[] getParamAnnotations() {
        return paramAnnotations;
    }

    public Param getParam() {
        return param;
    }

    private void setParam(Param param) {
        this.param = param;
    }

    public Dynamic getDynamic() {
        return dynamic;
    }

    private void setDynamic(Dynamic dynamic) {
        this.dynamic = dynamic;
    }

    public void setParamIndex(int paramIndex) {
        this.paramIndex = paramIndex;
    }

    public void setParamType(Class<?> paramType) {
        this.paramType = paramType;
    }

    public Class<?> getParamType() {
        return paramType;
    }

    public int getParamIndex() {
        return paramIndex;
    }

    public int getSeqParamIndex() {
        return seqParamIndex;
    }

    public int getSeqDynamicIndex() {
        return seqDynamicIndex;
    }

    public void setSeqParamIndex(int seqParamIndex) {
        this.seqParamIndex = seqParamIndex;
    }

    public void setSeqDynamicIndex(int seqDynamicIndex) {
        this.seqDynamicIndex = seqDynamicIndex;
    }

    public ReturnType getReturnType() {
        return returnType;
    }

    public void setReturnType(ReturnType returnType) {
        this.returnType = returnType;
    }

    public void setParamAnnotations(Annotation[] paramAnnotations) {
        this.paramAnnotations = paramAnnotations;
        setParam(findAnnotation(paramAnnotations, Param.class));
        setDynamic(findAnnotation(paramAnnotations, Dynamic.class));
        setSqlId(findAnnotation(paramAnnotations, SqlId.class));
        setReturnType(findAnnotation(paramAnnotations, ReturnType.class));
    }

    private <T extends Annotation> T findAnnotation(Annotation[] paramAnnotations, Class<T> annotationType) {
        for (Annotation paramAnnotation : paramAnnotations) {
            if (annotationType.isInstance(paramAnnotation)) return (T) paramAnnotation;
        }

        return null;
    }

    public void setSqlId(SqlId sqlId) {
        this.sqlId = sqlId;
    }

    public SqlId getSqlId() {
        return sqlId;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public int getVarIndex() {
        return getParamIndex() + 1 + getOffset();
    }
}
