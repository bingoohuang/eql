package org.n3r.eql.convert;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@EqlConvert(DayStringConverter.class)
public @interface DayString {
    String format();
}
