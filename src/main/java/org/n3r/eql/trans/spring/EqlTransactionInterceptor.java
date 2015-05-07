package org.n3r.eql.trans.spring;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class EqlTransactionInterceptor implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        EqlTransactionManager.start();
        try {
            Object retValue = invocation.proceed();
            EqlTransactionManager.commit();
            return retValue;
        } catch (Throwable throwable) {
            EqlTransactionManager.rollback();
            throw throwable;
        } finally {
            EqlTransactionManager.clear();
        }
    }
}
