package org.n3r.eql.convert;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/2/6.
 */
public class StripConverter implements EqlConverter {
    @Override public Object convert(Annotation ann, Object src) {
        if (src == null) return null;

        Strip stripAnn = (Strip) ann;
        String value = String.valueOf(src);

        String stripped = value;
        if (StringUtils.isNotEmpty(stripAnn.value())) {
            stripped = StringUtils.removeStart(stripped, stripAnn.value());
            stripped = StringUtils.removeEnd(stripped, stripAnn.value());
        }
        if (StringUtils.isNotEmpty(stripAnn.left())) {
            stripped = StringUtils.removeStart(stripped, stripAnn.left());
        }
        if (StringUtils.isNotEmpty(stripAnn.right())) {
            stripped = StringUtils.removeEnd(stripped, stripAnn.right());
        }

        return stripped;
    }
}
