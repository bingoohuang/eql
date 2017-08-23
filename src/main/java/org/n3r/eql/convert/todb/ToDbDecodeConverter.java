package org.n3r.eql.convert.todb;

import lombok.val;
import org.n3r.eql.convert.DecodeUtils;

import java.lang.annotation.Annotation;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/2/9.
 */
public class ToDbDecodeConverter implements ToDbConverter {
    @Override public Object convert(Annotation ann, Object src) {
        if (src == null) return null;

        val toDbDecode = (ToDbDecode) ann;
        val srcStr = String.valueOf(src);
        val decodeValues = toDbDecode.value();
        val toType = toDbDecode.toType();
        return DecodeUtils.decode(srcStr, decodeValues, toType);
    }
}
