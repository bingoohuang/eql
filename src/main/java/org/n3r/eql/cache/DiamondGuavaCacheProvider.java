package org.n3r.eql.cache;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.n3r.diamond.client.Miner;
import org.n3r.diamond.client.Minerable;
import org.n3r.eql.impl.EqlUniqueSqlId;

import java.util.concurrent.Callable;
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

        String sqlIdVersion = getSqlIdCacheVersion(uniqueSQLId);
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
                    new Callable<Cache<EqlCacheKey, Optional<Object>>>() {
                        @Override
                        public Cache<EqlCacheKey, Optional<Object>> call() throws Exception {
                            String sqlIdVersion = getSqlIdCacheVersion(uniqueSQLId);
                            cachEQLIdVersion.put(uniqueSQLId, Optional.fromNullable(sqlIdVersion));
                            Cache<EqlCacheKey, Optional<Object>> subCache = CacheBuilder.newBuilder().build();
                            return subCache;
                        }
                    });

            subCache.put(cacheKey, Optional.fromNullable(result));
        } catch (ExecutionException e) {
            // should not happened
        }
    }

    private String getSqlIdCacheVersion(EqlUniqueSqlId uniquEQLId) {
        final String dataId = uniquEQLId.getSqlClassPath().replaceAll("/", ".");
        Minerable sqlFileProperties = new Miner().getMiner(EQL_CACHE, dataId);
        String key = uniquEQLId.getSqlId() + ".cacheVersion";
        return sqlFileProperties.getString(key);
    }

}
