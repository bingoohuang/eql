package org.n3r.eql.eqler.annotations;

import org.n3r.eql.eqler.OnErr;

import java.lang.annotation.*;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/20.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SqlOptions {
    boolean iterate() default false;

    OnErr onErr() default OnErr.Unset;

    String split() default "";

    String value() default "";
}
