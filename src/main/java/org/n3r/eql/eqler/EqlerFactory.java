package org.n3r.eql.eqler;

import com.github.bingoohuang.westcache.WestCacheFactory;
import com.github.bingoohuang.westcache.utils.Anns;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.n3r.eql.eqler.generators.ClassGenerator;
import org.n3r.eql.ex.EqlConfigException;
import org.n3r.eql.util.C;

import java.util.concurrent.Callable;

@UtilityClass
@SuppressWarnings("unchecked")
public class EqlerFactory {
    private Cache<Class, Object> eqlerCache = CacheBuilder.newBuilder().build();

    @SneakyThrows
    public <T> T getEqler(final Class<T> eqlerClass) {
        ensureClassIsAnInterface(eqlerClass);
        return (T) eqlerCache.get(eqlerClass, new Callable<Object>() {
            @Override public Object call() {
                return loadEqler(eqlerClass);
            }
        });
    }

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

    @SneakyThrows
    private <T> T createObject(Class<T> clazz) {
        return clazz.newInstance();
    }

    private <T> void ensureClassIsAnInterface(Class<T> clazz) {
        if (clazz.isInterface()) return;

        throw new EqlConfigException(clazz + " is not an interface");
    }
}
