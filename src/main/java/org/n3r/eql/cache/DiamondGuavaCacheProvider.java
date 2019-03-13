package org.n3r.eql.cache;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.n3r.diamond.client.Miner;
import org.n3r.eql.impl.EqlUniqueSqlId;

import java.util.concurrent.ExecutionException;

public class DiamondGuavaCacheProvider implements EqlCacheProvider {
    public static final String EQL_CACHE = "EQL.CACHE";
    Cache<EqlUniqueSqlId, Cache<EqlCacheKey, Optional<Object>>> cache
            = CacheBuilder.newBuilder().build();
    Cache<EqlUniqueSqlId, Optional<String>> cachEQLIdVersion
            = CacheBuilder.newBuilder().build();

    @Override
    public Optional<Object> getCache(EqlCacheKey cacheKey) {
        val uniqueSQLId = cacheKey.getUniqueSQLId();
        val cachedSqlIdVersion = cachEQLIdVersion.getIfPresent(uniqueSQLId);
        if (cachedSqlIdVersion == null) return null;

        val sqlIdVersion = getSqlIdCacheVersion(uniqueSQLId);
        if (!StringUtils.equals(sqlIdVersion, cachedSqlIdVersion.orNull())) {
            cache.invalidate(uniqueSQLId);
            cachEQLIdVersion.put(uniqueSQLId, Optional.fromNullable(sqlIdVersion));
            return null;
        }

        val subCache = cache.getIfPresent(uniqueSQLId);
        if (subCache == null) return null;

        return subCache.getIfPresent(cacheKey);
    }

    @Override
    public void setCache(final EqlCacheKey cacheKey, Object result) {
        val uniqueSQLId = cacheKey.getUniqueSQLId();
        try {
            val subCache = cache.get(uniqueSQLId,
                    () -> {
                        val sqlIdVersion = getSqlIdCacheVersion(uniqueSQLId);
                        cachEQLIdVersion.put(uniqueSQLId, Optional.fromNullable(sqlIdVersion));
                        return CacheBuilder.newBuilder().build();
                    });

            subCache.put(cacheKey, Optional.fromNullable(result));
        } catch (ExecutionException e) {
            // should not happened
        }
    }

    private String getSqlIdCacheVersion(EqlUniqueSqlId uniquEQLId) {
        val dataId = uniquEQLId.getSqlClassPath().replaceAll("/", ".");
        val sqlFileProperties = new Miner().getMiner(EQL_CACHE, dataId);
        val key = uniquEQLId.getSqlId() + ".cacheVersion";
        return sqlFileProperties.getString(key);
    }

}
