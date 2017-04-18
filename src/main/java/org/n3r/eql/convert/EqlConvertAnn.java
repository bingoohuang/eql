package org.n3r.eql.convert;

import java.lang.annotation.Annotation;

public class EqlConvertAnn<T> {
    public final T convert;
    public final Annotation annotation;

    public EqlConvertAnn(T convert, Annotation annotation) {
        this.convert = convert;
        this.annotation = annotation;
    }
}
