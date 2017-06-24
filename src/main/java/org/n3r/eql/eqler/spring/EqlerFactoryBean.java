package org.n3r.eql.eqler.spring;

import org.n3r.eql.eqler.EqlerFactory;
import org.springframework.beans.factory.FactoryBean;

public class EqlerFactoryBean<T> implements FactoryBean<T> {
    private Class<T> eqlerInterface;

    public void setEqlerInterface(Class<T> eqlerInterface) {
        this.eqlerInterface = eqlerInterface;
    }

    @Override
    public T getObject() throws Exception {
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
}
