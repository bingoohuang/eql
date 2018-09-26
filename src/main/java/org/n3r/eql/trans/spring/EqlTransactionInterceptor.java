package org.n3r.eql.trans.spring;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

@Slf4j
public class EqlTransactionInterceptor implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        EqlTransactionManager.start();
        try {
            Object retValue = invocation.proceed();
            EqlTransactionManager.commit();
            return retValue;
        } catch (Throwable throwable) {
            log.error("error", throwable);
            EqlTransactionManager.rollback();
            throw throwable;
        } finally {
            EqlTransactionManager.end();
        }
    }
}
