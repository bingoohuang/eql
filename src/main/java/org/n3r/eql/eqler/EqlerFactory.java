package org.n3r.eql.eqler;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.n3r.eql.eqler.enhancer.EqlerEnhancer;
import org.n3r.eql.eqler.generators.ClassGenerator;
import org.n3r.eql.ex.EqlConfigException;

import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@UtilityClass
@SuppressWarnings("unchecked")
public class EqlerFactory {
    private static List<EqlerEnhancer> enhancers;

    static {
        enhancers = StreamSupport
                .stream(ServiceLoader.load(EqlerEnhancer.class).spliterator(), false)
                .sorted(Comparator.comparingInt(EqlerEnhancer::getOrder))
                .collect(Collectors.toList());
    }

    private Cache<Class, Object> eqlerCache = CacheBuilder.newBuilder().build();

    @SneakyThrows
    public <T> T getEqler(final Class<T> eqlerClass) {
        ensureClassIsAnInterface(eqlerClass);
        return (T) eqlerCache.get(eqlerClass, () -> loadEqler(eqlerClass));
    }

    private Object loadEqler(Class eqlerClass) {
        val generator = new ClassGenerator(eqlerClass);
        val eqlImplClass = generator.generate();
        val implObjet = createObject(eqlImplClass);

        return wrapWithEnhancer(eqlerClass, implObjet);
    }

    private Object wrapWithEnhancer(Class eqlerClass, Object implObject) {
        Object enhancedObject = implObject;
        for (val enhancer : enhancers) {
            if (enhancer.isEnabled(eqlerClass)) {
                enhancedObject = enhancer.build(eqlerClass, enhancedObject);
            }
        }
        return enhancedObject;
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
