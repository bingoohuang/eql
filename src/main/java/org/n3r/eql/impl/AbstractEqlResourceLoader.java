package org.n3r.eql.impl;

import org.n3r.eql.base.DynamicLanguageDriver;
import org.n3r.eql.base.EqlResourceLoader;

public abstract class AbstractEqlResourceLoader implements EqlResourceLoader {
    protected DynamicLanguageDriver dynamicLanguageDriver;

    @Override
    public void setEqlLazyLoad(boolean eqlLazyLoad) {
        this.eqlLazyLoad = eqlLazyLoad;
    }

    protected boolean eqlLazyLoad;

    @Override
    public void setDynamicLanguageDriver(DynamicLanguageDriver dynamicLanguageDriver) {
        this.dynamicLanguageDriver = dynamicLanguageDriver;
    }

    @Override
    public DynamicLanguageDriver getDynamicLanguageDriver() {
        return dynamicLanguageDriver;
    }
}
