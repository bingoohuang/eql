package org.n3r.eql.impl;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.Resources;
import org.n3r.eql.parser.SqlBlock;
import org.n3r.eql.parser.SqlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class SqlResourceLoader {
    static Logger log = LoggerFactory.getLogger(SqlResourceLoader.class);
    static Cache<String, Optional<Map<String, SqlBlock>>> fileCache = CacheBuilder.newBuilder().build();
    static Cache<String, SqlBlock> sqlCache = CacheBuilder.newBuilder().build();

    public static SqlBlock load(String sqlClassPath, String sqlId) {
        load(sqlClassPath);

        SqlBlock sqlBlock = sqlCache.getIfPresent(cacheKey(sqlClassPath, sqlId));
        if (sqlBlock == null) throw new RuntimeException("unable to find sql id " + sqlId);

        return sqlBlock;
    }

    public static Map<String, SqlBlock> load(final String sqlClassPath) {
        Callable<Optional<Map<String, SqlBlock>>> valueLoader = new Callable<Optional<Map<String, SqlBlock>>>() {
            @Override
            public Optional<Map<String, SqlBlock>> call() throws Exception {
                String sqlContent = loadClassPathResource(sqlClassPath);
                if (sqlContent == null) {
                    log.warn("classpath sql {} not found", sqlClassPath);
                    return Optional.absent();
                }

                Map<String, SqlBlock> sqlBlocks = new SqlParser().parse(sqlClassPath, sqlContent);
                for (SqlBlock sqlBlock : sqlBlocks.values()) {
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
