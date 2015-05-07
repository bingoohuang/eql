package org.n3r.eql.trans.spring;

import org.aopalliance.aop.Advice;
import org.n3r.eql.trans.spring.annotation.EqlTranactional;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

/**
 * Created by liolay on 15-5-7.
 */
public class EqlTransactionAdvisor extends AbstractPointcutAdvisor {
    final StaticMethodMatcherPointcut pointcut = new StaticMethodMatcherPointcut() {

        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return method.isAnnotationPresent(EqlTranactional.class);
        }
    };

    @Autowired
    EqlTransactionInterceptor interceptor;

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public Advice getAdvice() {
        return this.interceptor;
    }

}
