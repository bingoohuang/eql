package org.n3r.eql.convert;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/2/6.
 */
public class TruncateTailConverter implements EqlConverter {
    @Override public Object convert(Annotation ann, Object src) {
        if (src == null) return null;

        TruncateTail truncateTailAnn = (TruncateTail) ann;
        String value = String.valueOf(src);
        return StringUtils.removeEnd(value, truncateTailAnn.value());
    }
}
