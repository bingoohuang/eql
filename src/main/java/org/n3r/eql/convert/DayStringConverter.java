package org.n3r.eql.convert;

import java.lang.annotation.Annotation;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class DayStringConverter implements EqlConverter {
    @Override
    public Object convert(Annotation ann, Object src) {
        if (src == null) return null;

        Timestamp ts = (Timestamp) src;
        DayString ds = (DayString) ann;

        SimpleDateFormat sdf = new SimpleDateFormat(ds.format());
        return sdf.format(ts);
    }
}
