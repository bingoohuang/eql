package org.n3r.eql.cache;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.n3r.eql.impl.EqlUniqueSqlId;
import org.n3r.eql.spec.ParamsAppliable;
import org.n3r.eql.spec.SpecParser;
import org.n3r.eql.util.KeyValue;

import java.util.concurrent.ExecutionException;

import static com.google.common.base.MoreObjects.firstNonNull;

@Slf4j
public class EqlCacheSettings {
    public static final String DEFAULT_GUAVA_CACHE_MODEL = "guava";
    public static final String DEFAULT_DIAMOND_GUAVA_CACHE_MODEL = "diamond-guava";
    private static Cache<EqlCacheModelKey, EqlCacheProvider> eqlCacheModels;

    static {
        eqlCacheModels = CacheBuilder.newBuilder().build();
    }

    public static EqlCacheProvider processCacheModel(
            String sqlClassPath,
            KeyValue cacheModelSetting) {
        return processCacheModel(sqlClassPath, cacheModelSetting, true);
    }

    public static EqlCacheProvider processCacheModel(
            String sqlClassPath,
            KeyValue cacheModelSetting,
            boolean addToCache) {
        if (!cacheModelSetting.keyStartsWith("impl")) return null;

        val implKeyValue = cacheModelSetting.removeKeyPrefix("impl");
        val cacheModelName = implKeyValue.getKey();
        val cacheModelImpl = implKeyValue.getValue();

        val spec = SpecParser.parseSpecLeniently(cacheModelImpl);
        try {
            val clazz = Class.forName(spec.getName());

            if (!EqlCacheProvider.class.isAssignableFrom(clazz)) {
                log.error("processCacheModel {} required to implement " +
                        "org.n3r.eql.cache.EqlCacheProvider", spec.getName());
                return null;
            }

            val impl = (EqlCacheProvider) clazz.newInstance();
            if (impl instanceof ParamsAppliable)
                ((ParamsAppliable) impl).applyParams(spec.getParams());

            if (addToCache)
                eqlCacheModels.put(new EqlCacheModelKey(sqlClassPath, cacheModelName), impl);

            return impl;
        } catch (Exception e) {
            log.error("processCacheModel error", e);
        }

        return null;
    }

    public static EqlCacheProvider getCacheProvider(
            EqlUniqueSqlId uniqueSQLId, String cacheModel) {
        val model = firstNonNull(cacheModel, EqlCacheSettings.DEFAULT_GUAVA_CACHE_MODEL);

        val cacheModelKey = new EqlCacheModelKey(uniqueSQLId.getSqlClassPath(), model);
        val provider = eqlCacheModels.getIfPresent(cacheModelKey);
        if (provider != null) return provider;

        if (DEFAULT_GUAVA_CACHE_MODEL.equals(model))
            return createDefaultGuavaCacheModel(uniqueSQLId.getSqlClassPath(), cacheModelKey);
        if (DEFAULT_DIAMOND_GUAVA_CACHE_MODEL.equals(model))
            return createDefaultDiamondGuavaCacheModel(uniqueSQLId.getSqlClassPath(), cacheModelKey);

        log.warn("unable to find cache provider by cache model {}", model);

        return null;
    }

    private static EqlCacheProvider createDefaultGuavaCacheModel(
            String sqlClassPath,
            EqlCacheModelKey cacheModelKey) {
        val settingKey = "impl." + DEFAULT_GUAVA_CACHE_MODEL;
        val settingVal = "@org.n3r.eql.cache.GuavaCacheProvider(\"expireAfterWrite=1d\")";
        val setting = new KeyValue(settingKey, settingVal);

        return createCacheModel(sqlClassPath, cacheModelKey, setting);
    }

    private static EqlCacheProvider createDefaultDiamondGuavaCacheModel(
            String sqlClassPath,
            EqlCacheModelKey cacheModelKey) {
        val settingKey = "impl." + DEFAULT_DIAMOND_GUAVA_CACHE_MODEL;
        val settingVal = "@org.n3r.eql.cache.DiamondGuavaCacheProvider";
        val setting = new KeyValue(settingKey, settingVal);

        return createCacheModel(sqlClassPath, cacheModelKey, setting);
    }

    private static EqlCacheProvider createCacheModel(
            final String sqlClassPath,
            EqlCacheModelKey cacheModelKey,
            final KeyValue setting) {
        try {
            return eqlCacheModels.get(cacheModelKey, () -> processCacheModel(sqlClassPath, setting, false));
        } catch (ExecutionException e) {
            log.warn("create default cache model error", e.getCause());
        }

        return null;
    }

    @Value
    static class EqlCacheModelKey {
        private String sqlClassPath;
        private String modelName;
    }
}
