package org.n3r.eql.convert;

import java.lang.annotation.Annotation;

public class UpperConverter implements EqlConverter {
    @Override
    public Object convert(Annotation ann, Object src) {
        if (src == null) return null;

        return ((String) src).toUpperCase();
    }
}
