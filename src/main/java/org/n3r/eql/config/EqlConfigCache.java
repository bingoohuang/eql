package org.n3r.eql.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.n3r.eql.Eql;

public class EqlConfigCache {
    static LoadingCache<String, EqlConfig> eqlConfigLocal
            = CacheBuilder.newBuilder().build(new CacheLoader<String, EqlConfig>() {
        @Override
        public EqlConfig load(String key) throws Exception {
            return EqlConfigFactory.parseConfig(key);
        }
    });

    public static void putAsDefault(EqlConfig eqlConfig) {
        put(Eql.DEFAULT_CONN_NAME, eqlConfig);
    }

    public static void put(String eqlConfigName, EqlConfig eqlConfig) {
        eqlConfigLocal.put(eqlConfigName, eqlConfig);
    }

    public static EqlConfig getEqlConfig(String eqlConfigName) {
        return eqlConfigLocal.getUnchecked(eqlConfigName);
    }
}
