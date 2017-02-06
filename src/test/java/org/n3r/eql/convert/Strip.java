package org.n3r.eql.convert;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/2/6.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@EqlConvert(StripConverter.class)
public @interface Strip {
    /**
     * Value need to be stripped from two ends.
     *
     * @return value set.
     */
    String value() default "";

    /**
     * Value need to be stripped from left.
     *
     * @return left stripped value.
     */
    String left() default "";

    /**
     * Value need to be stripped from right.
     *
     * @return right stripped value.
     */
    String right() default "";
}
