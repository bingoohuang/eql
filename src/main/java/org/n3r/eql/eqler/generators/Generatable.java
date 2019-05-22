package org.n3r.eql.eqler.generators;

import lombok.val;
import org.n3r.eql.eqler.annotations.EqlerConfig;

import java.lang.reflect.AnnotatedElement;

public interface Generatable {
    void generate();

    static EqlerConfig parseEqlerConfig(AnnotatedElement annotatedElement) {
        val eqlerConfig = annotatedElement.getAnnotation(EqlerConfig.class);
        if (null != eqlerConfig) return eqlerConfig;

        val annotations = annotatedElement.getAnnotations();
        for (val annotation : annotations) {
            if (annotation.toString().startsWith("@java.lang.")) continue;
            val annotationEqlerConfig = annotation
                    .annotationType().getAnnotation(EqlerConfig.class);
            if (null != annotationEqlerConfig) return annotationEqlerConfig;
        }

        return null;
    }
}
