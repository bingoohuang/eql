package org.n3r.eql.trans.spring;

import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;

public class EqlTransactionConfig {

    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }

    @Bean
    public EqlTransactionAdvisor EqlTranAdvisorCreator(){
        return new EqlTransactionAdvisor();
    }


    @Bean
    public EqlTransactionInterceptor EqlTranInterceptorCreator(){
        return new EqlTransactionInterceptor();
    }
}
