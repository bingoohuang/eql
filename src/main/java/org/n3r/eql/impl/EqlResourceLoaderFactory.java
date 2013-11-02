package org.n3r.eql.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.n3r.eql.base.EqlResourceLoader;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.joor.Reflect;
import org.n3r.eql.parser.EqlBlock;
import org.n3r.eql.util.EqlUtils;

public class EqlResourceLoaderFactory {
    static LoadingCache<EqlConfig, EqlResourceLoader> loaderCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<EqlConfig, EqlResourceLoader>() {
                @Override
                public EqlResourceLoader load(EqlConfig eqlConfig) throws Exception {
                    String loader = eqlConfig.getStr("sql.resource.loader");

                    EqlResourceLoader eqlResourceLoader =
                            EqlUtils.isBlank(loader) ? new FileEqlResourceLoader()
                                    : Reflect.on(loader).create().<EqlResourceLoader>get();

                    eqlResourceLoader.initialize(eqlConfig);

                    return eqlResourceLoader;
                }
            });

    public static EqlBlock load(EqlConfig eqlConfig, String sqlClassPath, String sqlId) {
        EqlResourceLoader eqlResourceLoader = loaderCache.getUnchecked(eqlConfig);
        return eqlResourceLoader.loadEqlBlock(sqlClassPath, sqlId);
    }

}
