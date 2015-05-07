package org.n3r.eql.trans.spring;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Created by liolay on 15-5-7.
 */
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
            throwable.printStackTrace();
            throw throwable;
        } finally {
            EqlTransactionManager.clear();
        }
    }
}
