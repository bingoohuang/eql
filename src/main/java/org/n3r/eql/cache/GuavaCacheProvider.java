package org.n3r.eql.cache;


import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.n3r.eql.spec.ParamsAppliable;

public class GuavaCacheProvider implements EqlCacheProvider, ParamsAppliable {
    private Cache<EqlCacheKey, Optional<Object>> guavaCache;

    @Override
    public Optional<Object> getCache(EqlCacheKey cacheKey) {
        return guavaCache.getIfPresent(cacheKey);
    }

    @Override
    public void setCache(EqlCacheKey cacheKey, Object result) {
        guavaCache.put(cacheKey, Optional.fromNullable(result));
    }

    @Override
    public void applyParams(String[] params) {
        String spec = params[0];
        guavaCache = CacheBuilder.from(spec).build();
    }
}
