package org.n3r.eql.convert.todb;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/2/9.
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ToDbConvert {
    Class<? extends ToDbConverter>[] value();
}
