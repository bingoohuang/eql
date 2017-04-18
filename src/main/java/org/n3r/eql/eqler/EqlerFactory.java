package org.n3r.eql.eqler;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.val;
import org.n3r.eql.eqler.generators.ClassGenerator;
import org.n3r.eql.ex.EqlConfigException;

public class EqlerFactory {
    private static LoadingCache<Class, Object> eqlerCache =
            CacheBuilder.newBuilder().build(new CacheLoader<Class, Object>() {
                @Override
                public Object load(Class eqlerClass) throws Exception {
                    val generator = new ClassGenerator(eqlerClass);
                    Class<?> eqlImplClass = generator.generate();
                    return createObject(eqlImplClass);
                }
            });

    public static <T> T getEqler(final Class<T> eqlerClass) {
        ensureClassIsAnInterface(eqlerClass);
        return (T) eqlerCache.getUnchecked(eqlerClass);
    }

    @SneakyThrows
    private static <T> T createObject(Class<T> clazz) {
        return clazz.newInstance();
    }

    private static <T> void ensureClassIsAnInterface(Class<T> clazz) {
        if (clazz.isInterface()) return;

        throw new EqlConfigException(clazz + " is not an interface");
    }
}
