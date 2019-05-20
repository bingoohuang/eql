package org.n3r.eql.eqler;

import org.n3r.eql.eqler.annotations.EqlerConfig;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@EqlerConfig(value = "mysql"/*, eql= Dql.class*/)
public @interface MyEqlerConfig {
}
