package org.n3r.eql.impl;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.n3r.eql.base.DynamicLanguageDriver;
import org.n3r.eql.base.EqlResourceLoader;
import org.n3r.eql.parser.EqlBlock;
import org.n3r.eql.parser.EqlParser;
import org.n3r.eql.util.EqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class FileEqlResourceLoader implements EqlResourceLoader {
    static Logger log = LoggerFactory.getLogger(FileEqlResourceLoader.class);
    static Cache<String, Optional<Map<String, EqlBlock>>> fileCache = CacheBuilder.newBuilder().build();
    static Cache<String, EqlBlock> sqlCache = CacheBuilder.newBuilder().build();
    private DynamicLanguageDriver dynamicLanguageDriver;

    public FileEqlResourceLoader() {
    }

    @Override
    public EqlBlock loadEqlBlock(String sqlClassPath, String sqlId) {
        load(this, sqlClassPath);

        EqlBlock eqlBlock = sqlCache.getIfPresent(EqlUtils.uniqueSqlId(sqlClassPath, sqlId));
        if (eqlBlock != null) return eqlBlock;

        throw new RuntimeException("unable to find sql id " + sqlId);
    }

    @Override
    public Map<String, EqlBlock> load(String classPath) {
        return load(this, classPath);
    }

    @Override
    public void setDynamicLanguageDriver(DynamicLanguageDriver dynamicLanguageDriver) {
        this.dynamicLanguageDriver = dynamicLanguageDriver;
    }

    @Override
    public DynamicLanguageDriver getDynamicLanguageDriver() {
        return dynamicLanguageDriver;
    }

    public static Map<String, EqlBlock> load(final EqlResourceLoader eqlResourceLoader,
                                             final String sqlClassPath) {
        Callable<Optional<Map<String, EqlBlock>>> valueLoader = new Callable<Optional<Map<String, EqlBlock>>>() {
            @Override
            public Optional<Map<String, EqlBlock>> call() throws Exception {
                String sqlContent = EqlUtils.classResourceToString(sqlClassPath);
                if (sqlContent == null) {
                    log.warn("classpath sql {} not found", sqlClassPath);
                    return Optional.absent();
                }

                return Optional.of(updateBlockCache(sqlContent, eqlResourceLoader, sqlClassPath));
            }
        };

        try {
            return fileCache.get(sqlClassPath, valueLoader).orNull();
        } catch (ExecutionException e) {
            Throwables.propagateIfPossible(Throwables.getRootCause(e));
        }
        return null;
    }

    public static Map<String, EqlBlock> updateBlockCache(String sqlContent,
                                                         EqlResourceLoader eqlResourceLoader,
                                                         String sqlClassPath) {
        Map<String, EqlBlock> sqlBlocks = new EqlParser(eqlResourceLoader, sqlClassPath).parse(sqlContent);
        for (EqlBlock sqlBlock : sqlBlocks.values()) {
            String key = EqlUtils.uniqueSqlId(sqlClassPath, sqlBlock.getSqlId());
            sqlCache.put(key, sqlBlock);
        }
        return sqlBlocks;
    }


}
