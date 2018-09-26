package org.n3r.eql.eqler.spring;

import lombok.Setter;
import lombok.val;
import org.n3r.eql.eqler.EqlerFactory;
import org.n3r.eql.eqler.generators.ActiveProfilesThreadLocal;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class EqlerFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware {
    @Setter private Class<T> eqlerInterface;
    @Setter private ApplicationContext applicationContext;

    @Override
    public T getObject() {
        val activeProfiles = applicationContext.getEnvironment().getActiveProfiles();
        ActiveProfilesThreadLocal.set(activeProfiles);
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
