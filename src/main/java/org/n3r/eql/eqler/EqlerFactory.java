package org.n3r.eql.eqler;

import com.github.bingoohuang.westcache.WestCacheFactory;
import com.github.bingoohuang.westcache.utils.Anns;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.n3r.eql.eqler.generators.ClassGenerator;
import org.n3r.eql.ex.EqlConfigException;
import org.n3r.eql.util.C;

@UtilityClass
@SuppressWarnings("unchecked")
public class EqlerFactory {
    private LoadingCache<Class, Object> eqlerCache =
            CacheBuilder.newBuilder().build(new CacheLoader<Class, Object>() {
                @Override
                public Object load(Class eqlerClass) {
                    return loadEqler(eqlerClass);
                }
            });

    private Object loadEqler(Class eqlerClass) {
        val generator = new ClassGenerator(eqlerClass);
        val eqlImplClass = generator.generate();
        val implObjet = createObject(eqlImplClass);

        return wrapWestCacheable(eqlerClass, implObjet);
    }

    private Object wrapWestCacheable(Class eqlerClass, Object implObjet) {
        val className = "com.github.bingoohuang.westcache.WestCacheFactory";
        if (C.classExists(className)
                && Anns.isFastWestCacheAnnotated(eqlerClass)) {
            return WestCacheFactory.create(implObjet);
        }

        return implObjet;
    }

    public <T> T getEqler(final Class<T> eqlerClass) {
        ensureClassIsAnInterface(eqlerClass);
        return (T) eqlerCache.getUnchecked(eqlerClass);
    }

    @SneakyThrows
    private <T> T createObject(Class<T> clazz) {
        return clazz.newInstance();
    }

    private <T> void ensureClassIsAnInterface(Class<T> clazz) {
        if (clazz.isInterface()) return;

        throw new EqlConfigException(clazz + " is not an interface");
    }
}
