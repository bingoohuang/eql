package org.n3r.eql.cache;


import com.google.common.base.Optional;

public interface EqlCacheProvider {
    Optional<Object> getCache(EqlCacheKey cacheKey);

    void setCache(EqlCacheKey cacheKey, Object result);
}
