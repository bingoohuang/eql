package org.n3r.eql.impl;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.Resources;
import org.n3r.eql.parser.EqlParser;
import org.n3r.eql.parser.EqlBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class SqlResourceLoader {
    static Logger log = LoggerFactory.getLogger(SqlResourceLoader.class);
    static Cache<String, Optional<Map<String, EqlBlock>>> fileCache = CacheBuilder.newBuilder().build();
    static Cache<String, EqlBlock> sqlCache = CacheBuilder.newBuilder().build();

    public static EqlBlock load(String sqlClassPath, String sqlId) {
        load(sqlClassPath);

        EqlBlock eqlBlock = sqlCache.getIfPresent(cacheKey(sqlClassPath, sqlId));
        if (eqlBlock == null) throw new RuntimeException("unable to find sql id " + sqlId);

        return eqlBlock;
    }

    public static Map<String, EqlBlock> load(final String sqlClassPath) {
        Callable<Optional<Map<String, EqlBlock>>> valueLoader = new Callable<Optional<Map<String, EqlBlock>>>() {
            @Override
            public Optional<Map<String, EqlBlock>> call() throws Exception {
                String sqlContent = loadClassPathResource(sqlClassPath);
                if (sqlContent == null) {
                    log.warn("classpath sql {} not found", sqlClassPath);
                    return Optional.absent();
                }

                Map<String, EqlBlock> sqlBlocks = new EqlParser().parse(sqlClassPath, sqlContent);
                for (EqlBlock sqlBlock : sqlBlocks.values()) {
                    String key = cacheKey(sqlClassPath, sqlBlock.getSqlId());
                    sqlCache.put(key, sqlBlock);
                }

                return Optional.of(sqlBlocks);
            }
        };

        try {
            return fileCache.get(sqlClassPath, valueLoader).orNull();
        } catch (ExecutionException e) {
            Throwables.propagateIfPossible(Throwables.getRootCause(e));
        }
        return null;
    }


    private static String cacheKey(String sqlClassPath, String sqlId) {
        return sqlClassPath + ":" + sqlId;
    }

    public static String loadClassPathResource(String classPath) {
        ClassLoader loader = Objects.firstNonNull(
                Thread.currentThread().getContextClassLoader(),
                SqlResourceLoader.class.getClassLoader());
        URL url = loader.getResource(classPath);
        if (url == null) return null;

        try {
            return Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {

        }

        return null;
    }

}
