package org.n3r.eql.eqler.enhancer;

public interface EqlerEnhancer {

    boolean isEnabled(Class eqlerClass);

    Object build(Class eqlerClass, Object implObject);
}
