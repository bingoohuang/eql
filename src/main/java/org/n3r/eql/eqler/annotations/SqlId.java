package org.n3r.eql.eqler.annotations;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SqlId {
    String value() default "";
}
