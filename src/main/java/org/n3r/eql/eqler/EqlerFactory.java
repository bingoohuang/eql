package org.n3r.eql.eqler;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.n3r.eql.eqler.generators.ClassGenerator;
import org.n3r.eql.ex.EqlConfigException;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.util.Fucks;

public class EqlerFactory {
    private static LoadingCache<Class, Object> eqlerCache =
            CacheBuilder.newBuilder().build(new CacheLoader<Class, Object>() {
                @Override
                public Object load(Class eqlerClass) throws Exception {
                    ClassGenerator generator = new ClassGenerator(eqlerClass);
                    Class<?> eqlImplClass = generator.generate();
                    return createObject(eqlImplClass);
                }
            });

    public static <T> T getEqler(final Class<T> eqlerClass) {
        ensureEqlerClassIsAnInterface(eqlerClass);
        return (T) eqlerCache.getUnchecked(eqlerClass);
    }

    private static <T> T createObject(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw Fucks.fuck(e);
        }
    }

    private static <T> void ensureEqlerClassIsAnInterface(Class<T> eqlerClass) {
        if (eqlerClass.isInterface()) return;

        throw new EqlConfigException(eqlerClass + " is not an interface");
    }
}
