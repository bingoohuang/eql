package org.n3r.eql.convert;

import lombok.val;

import java.lang.annotation.Annotation;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/2/9.
 */
public class EqlDecodeConverter implements EqlConverter {
    @Override public Object convert(Annotation ann, Object src) {
        if (src == null) return null;

        val eqlDecode = (EqlDecode) ann;
        val srcStr = String.valueOf(src);
        val decodeValues = eqlDecode.value();
        val toType = eqlDecode.toType();
        return DecodeUtils.decode(srcStr, decodeValues, toType);
    }
}
