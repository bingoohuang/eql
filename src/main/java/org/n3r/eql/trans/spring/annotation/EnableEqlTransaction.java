package org.n3r.eql.trans.spring.annotation;

import org.n3r.eql.trans.spring.EqlTransactionConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by liolay on 15-5-7.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(EqlTransactionConfig.class)
public @interface EnableEqlTransaction {
}
