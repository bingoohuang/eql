package org.n3r.eql.eqler.annotations;

import java.lang.annotation.*;

@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
    String value();
}
