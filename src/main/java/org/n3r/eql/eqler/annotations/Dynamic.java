package org.n3r.eql.eqler.annotations;

import java.lang.annotation.*;

@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Dynamic {
    /**
     * 是否只能做dynamic参数使用。
     *
     * @return 是否只能做dynamic参数使用
     */
    boolean sole() default true;

    /**
     * 用做命名dynamic参数
     *
     * @return 用做命名dynamic参数
     */
    String value() default "";

    /**
     * alias for value().
     *
     * @return alias for value().
     */
    String name() default "";
}
