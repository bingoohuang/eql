package org.n3r.eql.convert;

import java.lang.annotation.Annotation;

public class EqlConvertAnn {
    public final EqlConvert convert;
    public final Annotation annotation;

    public EqlConvertAnn(EqlConvert convert, Annotation annotation) {
        this.convert = convert;
        this.annotation = annotation;
    }
}
