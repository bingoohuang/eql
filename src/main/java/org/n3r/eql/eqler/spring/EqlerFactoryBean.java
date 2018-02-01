package org.n3r.eql.eqler.spring;

import org.n3r.eql.eqler.EqlerFactory;
import org.n3r.eql.eqler.generators.ApplicationContextThreadLocal;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class EqlerFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware {
    private Class<T> eqlerInterface;
    private ApplicationContext applicationContext;

    public void setEqlerInterface(Class<T> eqlerInterface) {
        this.eqlerInterface = eqlerInterface;
    }

    @Override
    public T getObject() {
        ApplicationContextThreadLocal.set(applicationContext);
        return EqlerFactory.getEqler(eqlerInterface);
    }

    @Override
    public Class<?> getObjectType() {
        return this.eqlerInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
