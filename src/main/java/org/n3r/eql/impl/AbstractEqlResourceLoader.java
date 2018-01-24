package org.n3r.eql.impl;

import lombok.Getter;
import lombok.Setter;
import org.n3r.eql.base.DynamicLanguageDriver;
import org.n3r.eql.base.EqlResourceLoader;

public abstract class AbstractEqlResourceLoader implements EqlResourceLoader {
    @Getter @Setter protected DynamicLanguageDriver dynamicLanguageDriver;
    @Setter protected boolean eqlLazyLoad;
}
