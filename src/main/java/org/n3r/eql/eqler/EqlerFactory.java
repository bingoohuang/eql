package org.n3r.eql.eqler;

import com.github.bingoohuang.westcache.WestCacheFactory;
import com.github.bingoohuang.westcache.utils.Anns;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.val;
import org.n3r.eql.eqler.generators.ClassGenerator;
import org.n3r.eql.ex.EqlConfigException;
import org.n3r.eql.util.C;

public class EqlerFactory {
    private static LoadingCache<Class, Object> eqlerCache =
            CacheBuilder.newBuilder().build(new CacheLoader<Class, Object>() {
                @Override
                public Object load(Class eqlerClass) throws Exception {
                    val generator = new ClassGenerator(eqlerClass);
                    Class<?> eqlImplClass = generator.generate();
                    val implObjet = createObject(eqlImplClass);

                    return wrapWestCacheable(eqlerClass, implObjet);
                }
            });

    private static Object wrapWestCacheable(Class eqlerClass, Object implObjet) {
        if (C.classExists("com.github.bingoohuang.westcache.WestCacheFactory")
                && Anns.isFastWestCacheAnnotated(eqlerClass)) {
            return WestCacheFactory.create(implObjet);
        }

        return implObjet;
    }

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
