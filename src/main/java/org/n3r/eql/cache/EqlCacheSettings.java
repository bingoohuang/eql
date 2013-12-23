package org.n3r.eql.cache;


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
    public static final String defaultCacheModel = "guava";
    private static Cache<EqlCacheModelKey, EqlCacheProvider> eqlCacheModels;

    static {
        eqlCacheModels = CacheBuilder.newBuilder().build();
    }

    public static EqlCacheProvider processCacheModel(String sqlClassPath, KeyValue keyValue) {
        return processCacheModel(sqlClassPath, keyValue, true);
    }

    public static EqlCacheProvider processCacheModel(String sqlClassPath, KeyValue keyValue, boolean addToCache) {
        if (keyValue.keyStartsWith("impl")) {
            KeyValue implKeyValue = keyValue.removeKeyPrefix("impl");
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
        }

        return null;
    }

    public static EqlCacheProvider getCacheProvider(EqlUniqueSqlId uniqueSqlId, String model) {
        EqlCacheModelKey cacheModelKey = new EqlCacheModelKey(uniqueSqlId.getSqlClassPath(), model);
        EqlCacheProvider provider = eqlCacheModels.getIfPresent(cacheModelKey);
        if (provider != null) return provider;

        if (!defaultCacheModel.equals(model)) return null;

        return createDefaultCacheModel(uniqueSqlId.getSqlClassPath(), cacheModelKey);
    }

    private static EqlCacheProvider createDefaultCacheModel(final String sqlClassPath, EqlCacheModelKey cacheModelKey) {
        try {
            return eqlCacheModels.get(cacheModelKey, new Callable<EqlCacheProvider>() {
                @Override
                public EqlCacheProvider call() throws Exception {
                    String settingKey = "impl." + defaultCacheModel;
                    String settingVal = "@" + GuavaCacheProvider.class.getName() + "(\"expireAfterWrite=1d\")";
                    KeyValue settign = new KeyValue(settingKey, settingVal);
                    return processCacheModel(sqlClassPath, settign, false);
                }
            });
        } catch (ExecutionException e) {
            logger.warn("create default cache model error", e.getCause());
        }

        return null;
    }

    public static class EqlCacheModelKey {
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
