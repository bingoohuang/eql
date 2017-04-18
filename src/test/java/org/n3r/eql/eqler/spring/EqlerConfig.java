package org.n3r.eql.eqler.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"org.n3r.eql.eqler.spring"})
@EqlerScan("org.n3r.eql.eqler.spring")
public class EqlerConfig {

}
