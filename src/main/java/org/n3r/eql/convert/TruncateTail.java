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
@EqlConvert(TruncateTailConverter.class)
public @interface TruncateTail {
    /**
     * Value need to be truncated at tail.
     *
     * @return value set.
     */
    String value();
}
