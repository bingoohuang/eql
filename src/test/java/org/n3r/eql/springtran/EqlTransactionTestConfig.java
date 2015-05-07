package org.n3r.eql.springtran;

import org.n3r.eql.eqler.spring.EqlerScan;
import org.n3r.eql.trans.spring.annotation.EnableEqlTransaction;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableEqlTransaction
@EqlerScan(basePackageClasses = EqlTransactionTestConfig.class)
@ComponentScan(basePackageClasses = EqlTransactionTestConfig.class)
public class EqlTransactionTestConfig {

}
