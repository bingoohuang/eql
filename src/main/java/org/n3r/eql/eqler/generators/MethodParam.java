package org.n3r.eql.eqler.generators;

import lombok.Getter;
import lombok.Setter;
import org.n3r.eql.eqler.annotations.Dynamic;
import org.n3r.eql.eqler.annotations.Param;
import org.n3r.eql.eqler.annotations.ReturnType;
import org.n3r.eql.eqler.annotations.SqlId;

import java.lang.annotation.Annotation;

public class MethodParam {
    @Setter @Getter private int paramIndex;
    @Setter @Getter private Class<?> paramType;
    @Getter @Setter private Param param;
    @Getter @Setter private Dynamic dynamic;
    @Getter @Setter private SqlId sqlId;
    @Getter @Setter private ReturnType returnType;
    @Getter @Setter private int seqParamIndex = -1;
    @Getter @Setter private int seqDynamicIndex = -1;
    @Getter private Annotation[] paramAnnotations;
    @Getter @Setter private int offset;

    public void setParamAnnotations(Annotation[] paramAnnotations) {
        this.paramAnnotations = paramAnnotations;
        setParam(findAnnotation(paramAnnotations, Param.class));
        setDynamic(findAnnotation(paramAnnotations, Dynamic.class));
        setSqlId(findAnnotation(paramAnnotations, SqlId.class));
        setReturnType(findAnnotation(paramAnnotations, ReturnType.class));
    }

    @SuppressWarnings("unchecked")
    private <T extends Annotation> T findAnnotation(
            Annotation[] paramAnnotations, Class<T> annotationType) {
        for (Annotation paramAnnotation : paramAnnotations) {
            if (annotationType.isInstance(paramAnnotation))
                return (T) paramAnnotation;
        }

        return null;
    }

    public int getVarIndex() {
        return getParamIndex() + 1 + getOffset();
    }
}
