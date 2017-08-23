package org.n3r.eql.convert.todb;

import lombok.val;
import org.joda.time.format.DateTimeFormat;

import java.lang.annotation.Annotation;
import java.sql.Timestamp;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/2/10.
 */
public class ToDbTimestampConverter implements ToDbConverter {
    @Override public Object convert(Annotation ann, Object src) {
        if (src == null) return null;

        val toDbTimestamp = (ToDbTimestamp) ann;
        val formatter = DateTimeFormat.forPattern(toDbTimestamp.format());
        val dateTime = formatter.parseDateTime(String.valueOf(src));

        return new Timestamp(dateTime.getMillis());
    }
}
