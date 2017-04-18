package org.n3r.eql.convert;

import java.lang.annotation.Annotation;

public interface EqlConverter {
    Object convert(Annotation ann, Object src);
}
