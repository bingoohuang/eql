package org.n3r.eql.eqler.annotations;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProfiledSql {
    String profile();
    String[] sql();
}
