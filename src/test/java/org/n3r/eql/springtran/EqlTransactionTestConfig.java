package org.n3r.eql.springtran;

import org.n3r.eql.eqler.spring.EqlerScan;
import org.n3r.eql.trans.spring.annotation.EnableEqlTransaction;
import org.springframework.context.annotation.ComponentScan;

@EnableEqlTransaction
@EqlerScan
@ComponentScan
public class EqlTransactionTestConfig {

}
