package org.n3r.eql.eqler.annotations;

import org.n3r.eql.Eql;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EqlConfig {
    Class<? extends Eql> eql() default Eql.class;
    String value() default  "DEFAULT";
}
