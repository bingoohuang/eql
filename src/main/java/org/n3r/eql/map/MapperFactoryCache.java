package org.n3r.eql.map;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import lombok.val;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

public class MapperFactoryCache {
    private static LoadingCache<Class<?>, Optional<FromDbMapper>> fromDbMappers =
            CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, Optional<FromDbMapper>>() {
                @Override public Optional<FromDbMapper> load(Class<?> aClass) {
                    for (val mapper : fromDbMapperSet) {
                        if (mapper.support(aClass)) return Optional.of(mapper);
                    }
                    return Optional.empty();
                }
            });
    private static LoadingCache<Class<?>, Optional<ToDbMapper>> toDbMappers =
            CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, Optional<ToDbMapper>>() {
                @Override public Optional<ToDbMapper> load(Class<?> aClass) {
                    for (val mapper : toDbMapperSet) {
                        if (mapper.support(aClass)) return Optional.of(mapper);
                    }
                    return Optional.empty();
                }
            });


    private static Set<FromDbMapper> fromDbMapperSet = Sets.newHashSet();
    private static Set<ToDbMapper> toDbMapperSet = Sets.newHashSet();

    static {
        for (val mapperFactory : ServiceLoader.load(MapperFactory.class)) {
            mapperFactory.addFromDbMapper(fromDbMapperSet);
            mapperFactory.addToDbMapper(toDbMapperSet);
        }
    }

    public static Optional<FromDbMapper> getFromDbMapper(Class<?> aClass) {
        return fromDbMappers.getUnchecked(aClass);
    }

    public static Optional<ToDbMapper> getToDbMapper(Class<?> aClass) {
        return toDbMappers.getUnchecked(aClass);
    }
}
