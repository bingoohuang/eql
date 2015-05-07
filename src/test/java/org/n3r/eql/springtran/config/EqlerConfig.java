package org.n3r.eql.springtran.config;

import org.n3r.eql.eqler.spring.EqlerScan;
import org.n3r.eql.trans.spring.annotation.EnableEqlTransaction;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"org.n3r.eql.springtran"})
@EqlerScan("org.n3r.eql.springtran")
@EnableEqlTransaction
public class EqlerConfig {

}
