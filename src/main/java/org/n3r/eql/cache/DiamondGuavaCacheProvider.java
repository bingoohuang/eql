package org.n3r.eql.cache;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.StringUtils;
import org.n3r.diamond.client.DiamondMiner;
import org.n3r.eql.EqlPage;
import org.n3r.eql.impl.EqlUniqueSqlId;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class DiamondGuavaCacheProvider implements EqlCacheProvider {
    public static final String EQL_CACHE = "EQL.CACHE";
    Cache<EqlUniqueSqlId, Cache<EqlCacheSubKey, Optional<Object>>> cache
            = CacheBuilder.newBuilder().build();
    Cache<EqlUniqueSqlId, Optional<String>> cacheDiamond
            = CacheBuilder.newBuilder().build();

    @Override
    public Optional<Object> getCache(EqlCacheKey cacheKey) {
        final EqlUniqueSqlId uniqueSqlId = cacheKey.getUniqueSqlId();
        Optional<String> cachedSqlIdVersion = cacheDiamond.getIfPresent(uniqueSqlId);
        if (cachedSqlIdVersion == null) return null;

        String sqlIdVersion = getSqlIdCacheVersion(uniqueSqlId);
        if (!StringUtils.equals(sqlIdVersion, cachedSqlIdVersion.orNull())) {
            cache.invalidate(uniqueSqlId);
            cacheDiamond.put(uniqueSqlId, Optional.fromNullable(sqlIdVersion));
            return null;
        }

        Cache<EqlCacheSubKey, Optional<Object>> subCache = cache.getIfPresent(uniqueSqlId);
        if (subCache == null) return null;

        return subCache.getIfPresent(new EqlCacheSubKey(cacheKey));
    }

    @Override
    public void setCache(final EqlCacheKey cacheKey, Object result) {
        final EqlUniqueSqlId uniqueSqlId = cacheKey.getUniqueSqlId();
        try {
            Cache<EqlCacheSubKey, Optional<Object>> subCache = cache.get(uniqueSqlId,
                    new Callable<Cache<EqlCacheSubKey, Optional<Object>>>() {
                        @Override
                        public Cache<EqlCacheSubKey, Optional<Object>> call() throws Exception {
                            String sqlIdVersion = getSqlIdCacheVersion(uniqueSqlId);
                            cacheDiamond.put(uniqueSqlId, Optional.fromNullable(sqlIdVersion));
                            Cache<EqlCacheSubKey, Optional<Object>> subCache = CacheBuilder.newBuilder().build();
                            return subCache;
                        }
                    });

            subCache.put(new EqlCacheSubKey(cacheKey), Optional.fromNullable(result));
        } catch (ExecutionException e) {
            // should not happened
        }
    }

    private String getSqlIdCacheVersion(EqlUniqueSqlId uniqueSqlId) {
        final String dataId = uniqueSqlId.getSqlClassPath().replaceAll("/", ".");
        Properties sqlFileProperties = DiamondMiner.getProperties(EQL_CACHE, dataId);
        String key = uniqueSqlId.getSqlId() + ".cacheVersion";
        String property = sqlFileProperties.getProperty(key);
        return property;
    }

    static class EqlCacheSubKey {
        private Object[] params;
        private Object[] dynamics;
        private EqlPage page;

        EqlCacheSubKey(EqlCacheKey cacheKey) {
            this.params = cacheKey.getParams();
            this.dynamics = cacheKey.getDynamics();
            this.page = cacheKey.getPage();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EqlCacheSubKey that = (EqlCacheSubKey) o;

            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            if (!Arrays.equals(dynamics, that.dynamics)) return false;
            if (page != null ? !page.equals(that.page) : that.page != null) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            if (!Arrays.equals(params, that.params)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = params != null ? Arrays.hashCode(params) : 0;
            result = 31 * result + (dynamics != null ? Arrays.hashCode(dynamics) : 0);
            result = 31 * result + (page != null ? page.hashCode() : 0);
            return result;
        }

        public Object[] getParams() {
            return params;
        }

        public void setParams(Object[] params) {
            this.params = params;
        }

        public Object[] getDynamics() {
            return dynamics;
        }

        public void setDynamics(Object[] dynamics) {
            this.dynamics = dynamics;
        }

        public EqlPage getPage() {
            return page;
        }

        public void setPage(EqlPage page) {
            this.page = page;
        }
    }
}
