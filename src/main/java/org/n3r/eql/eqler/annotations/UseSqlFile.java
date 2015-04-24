package org.n3r.eql.eqler.annotations;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UseSqlFile {
    String value() default "";
    Class<?> clazz() default Void.class;
}
