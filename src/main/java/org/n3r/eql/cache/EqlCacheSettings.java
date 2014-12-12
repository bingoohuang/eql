package org.n3r.eql.cache;


import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.n3r.eql.impl.EqlUniqueSqlId;
import org.n3r.eql.spec.ParamsAppliable;
import org.n3r.eql.spec.Spec;
import org.n3r.eql.spec.SpecParser;
import org.n3r.eql.util.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class EqlCacheSettings {
    static Logger logger = LoggerFactory.getLogger(EqlCacheSettings.class);
    public static final String DEFAULT_GUAVA_CACHE_MODEL = "guava";
    public static final String DEFAULT_DIAMOND_GUAVA_CACHE_MODEL = "diamond-guava";
    private static Cache<EqlCacheModelKey, EqlCacheProvider> eqlCacheModels;

    static {
        eqlCacheModels = CacheBuilder.newBuilder().build();
    }

    public static EqlCacheProvider processCacheModel(String sqlClassPath,
                                                     KeyValue cacheModelSetting) {
        return processCacheModel(sqlClassPath, cacheModelSetting, true);
    }

    public static EqlCacheProvider processCacheModel(String sqlClassPath,
                                                     KeyValue cacheModelSetting,
                                                     boolean addToCache) {
        if (!cacheModelSetting.keyStartsWith("impl")) return null;

        KeyValue implKeyValue = cacheModelSetting.removeKeyPrefix("impl");
        String cacheModelName = implKeyValue.getKey();
        String cacheModelImpl = implKeyValue.getValue();

        Spec spec = SpecParser.parseSpecLeniently(cacheModelImpl);
        try {
            Class<?> clazz = Class.forName(spec.getName());

            if (!EqlCacheProvider.class.isAssignableFrom(clazz)) {
                logger.error("processCacheModel {} required to implement " +
                        "org.n3r.eql.cache.EqlCacheProvider", spec.getName());
                return null;
            }

            EqlCacheProvider impl = (EqlCacheProvider) clazz.newInstance();
            if (impl instanceof ParamsAppliable)
                ((ParamsAppliable) impl).applyParams(spec.getParams());

            if (addToCache)
                eqlCacheModels.put(new EqlCacheModelKey(sqlClassPath, cacheModelName), impl);

            return impl;
        } catch (Exception e) {
            logger.error("processCacheModel error", e);
        }

        return null;
    }

    public static EqlCacheProvider getCacheProvider(EqlUniqueSqlId uniquEQLId, String cacheModel) {
        String model = Objects.firstNonNull(cacheModel, EqlCacheSettings.DEFAULT_GUAVA_CACHE_MODEL);

        EqlCacheModelKey cacheModelKey = new EqlCacheModelKey(uniquEQLId.getSqlClassPath(), model);
        EqlCacheProvider provider = eqlCacheModels.getIfPresent(cacheModelKey);
        if (provider != null) return provider;

        if (DEFAULT_GUAVA_CACHE_MODEL.equals(model))
            return createDefaultGuavaCacheModel(uniquEQLId.getSqlClassPath(), cacheModelKey);
        if (DEFAULT_DIAMOND_GUAVA_CACHE_MODEL.equals(model))
            return createDefaultDiamondGuavaCacheModel(uniquEQLId.getSqlClassPath(), cacheModelKey);

        logger.warn("unable to find cache provider by cache model {}", model);

        return null;
    }

    private static EqlCacheProvider createDefaultGuavaCacheModel(String sqlClassPath,
                                                                 EqlCacheModelKey cacheModelKey) {
        String settingKey = "impl." + DEFAULT_GUAVA_CACHE_MODEL;
        String settingVal = "@org.n3r.eql.cache.GuavaCacheProvider(\"expireAfterWrite=1d\")";
        KeyValue setting = new KeyValue(settingKey, settingVal);

        return createCacheModel(sqlClassPath, cacheModelKey, setting);
    }

    private static EqlCacheProvider createDefaultDiamondGuavaCacheModel(String sqlClassPath,
                                                                        EqlCacheModelKey cacheModelKey) {
        String settingKey = "impl." + DEFAULT_DIAMOND_GUAVA_CACHE_MODEL;
        String settingVal = "@org.n3r.eql.cache.DiamondGuavaCacheProvider";
        KeyValue setting = new KeyValue(settingKey, settingVal);

        return createCacheModel(sqlClassPath, cacheModelKey, setting);
    }

    private static EqlCacheProvider createCacheModel(final String sqlClassPath,
                                                     EqlCacheModelKey cacheModelKey,
                                                     final KeyValue setting) {
        try {
            return eqlCacheModels.get(cacheModelKey, new Callable<EqlCacheProvider>() {
                @Override
                public EqlCacheProvider call() throws Exception {
                    return processCacheModel(sqlClassPath, setting, false);
                }
            });
        } catch (ExecutionException e) {
            logger.warn("create default cache model error", e.getCause());
        }

        return null;
    }

    static class EqlCacheModelKey {
        private String sqlClassPath;
        private String modelName;

        public EqlCacheModelKey(String sqlClassPath, String modelName) {
            this.sqlClassPath = sqlClassPath;
            this.modelName = modelName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EqlCacheModelKey that = (EqlCacheModelKey) o;

            if (!modelName.equals(that.modelName)) return false;
            if (!sqlClassPath.equals(that.sqlClassPath)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = sqlClassPath.hashCode();
            result = 31 * result + modelName.hashCode();
            return result;
        }
    }
}
