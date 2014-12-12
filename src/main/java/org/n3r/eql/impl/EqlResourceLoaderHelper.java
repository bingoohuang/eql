package org.n3r.eql.impl;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.n3r.eql.base.EqlResourceLoader;
import org.n3r.eql.parser.EqlBlock;
import org.n3r.eql.parser.EqlParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EqlResourceLoaderHelper {
    public static LoadingCache<EqlUniqueSqlId, Optional<EqlBlock>> buildSqlCache(
            final Cache<String, Optional<Map<String, EqlBlock>>> fileCache) {
        return CacheBuilder.newBuilder().build(
                new CacheLoader<EqlUniqueSqlId, Optional<EqlBlock>>() {
                    @Override
                    public Optional<EqlBlock> load(EqlUniqueSqlId eqlUniquEQLId) throws Exception {
                        return loadBlocks(fileCache, eqlUniquEQLId);
                    }
                }
        );
    }

    private static Optional<EqlBlock> loadBlocks(
            Cache<String, Optional<Map<String, EqlBlock>>> fileCache,
            EqlUniqueSqlId eqlUniquEQLId) {
        Optional<Map<String, EqlBlock>> blocks;
        blocks = fileCache.getIfPresent(eqlUniquEQLId.getSqlClassPath());
        if (!blocks.isPresent()) return Optional.absent();

        Map<String, EqlBlock> blockMap = blocks.get();
        EqlBlock eqlBlock = blockMap.get(eqlUniquEQLId.getSqlId());
        if (eqlBlock == null) return Optional.absent();

        eqlBlock.tryParsEQLs();

        return Optional.of(eqlBlock);
    }

    public static void updateBlockCache(String sqlContent,
                                        EqlResourceLoader eqlResourceLoader,
                                        String sqlClassPath,
                                        Cache<EqlUniqueSqlId, Optional<EqlBlock>> sqlCache,
                                        Cache<String, Optional<Map<String, EqlBlock>>> fileCache) {
        Optional<Map<String, EqlBlock>> oldBlocks = fileCache.getIfPresent(sqlClassPath);
        Set<String> oldSqlIds = oldBlocks.or(new HashMap<String, EqlBlock>()).keySet();

        EqlParser eqlParser = new EqlParser(eqlResourceLoader, sqlClassPath);
        Map<String, EqlBlock> sqlBlocks = eqlParser.parse(sqlContent);

        for (EqlBlock sqlBlock : sqlBlocks.values()) {
            EqlUniqueSqlId uniquEQLId = sqlBlock.getUniquEQLId();
            sqlCache.put(uniquEQLId, Optional.of(sqlBlock));
            oldSqlIds.remove(uniquEQLId.getSqlId());
        }

        for (String uniqueId : oldSqlIds) {
            sqlCache.invalidate(new EqlUniqueSqlId(sqlClassPath, uniqueId));
        }

        fileCache.put(sqlClassPath, Optional.of(sqlBlocks));
    }

    public static Map<String, EqlBlock> updateFileCache(String sqlContent,
                                                        EqlResourceLoader eqlResourceLoader,
                                                        String sqlClassPath, boolean eqlLazyLoad) {
        EqlParser eqlParser = new EqlParser(eqlResourceLoader, sqlClassPath);
        return eqlLazyLoad ? eqlParser.delayParse(sqlContent) : eqlParser.parse(sqlContent);
    }
}
