package org.n3r.eql.convert.todb;

import java.lang.annotation.Annotation;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/2/9.
 */
public interface ToDbConverter {
    Object convert(Annotation ann, Object src);
}
