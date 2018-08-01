package org.n3r.eql.impl;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.n3r.eql.base.EqlResourceLoader;
import org.n3r.eql.parser.EqlBlock;
import org.n3r.eql.parser.EqlParser;

import java.util.HashMap;
import java.util.Map;

@Slf4j @UtilityClass
public class EqlResourceLoaderHelper {
    public LoadingCache<EqlUniqueSqlId, Optional<EqlBlock>> buildSqlCache(
            final Cache<String, Optional<Map<String, EqlBlock>>> fileCache) {
        return CacheBuilder.newBuilder().build(new CacheLoader<EqlUniqueSqlId, Optional<EqlBlock>>() {
            @Override
            public Optional<EqlBlock> load(EqlUniqueSqlId eqlUniqueSqlId) {
                return loadBlocks(fileCache, eqlUniqueSqlId);
            }
        });
    }

    private Optional<EqlBlock> loadBlocks(
            Cache<String, Optional<Map<String, EqlBlock>>> fileCache,
            EqlUniqueSqlId sqlId) {
        val blocks = fileCache.getIfPresent(sqlId.getSqlClassPath());
        val eqlBlock = blocks.get().get(sqlId.getSqlId());
        if (eqlBlock == null) return Optional.absent();

        eqlBlock.tryParseSqls();
        return Optional.of(eqlBlock);
    }

    public void updateBlockCache(
            String sqlContent,
            EqlResourceLoader eqlResourceLoader,
            String sqlClassPath,
            Cache<EqlUniqueSqlId, Optional<EqlBlock>> sqlCache,
            Cache<String, Optional<Map<String, EqlBlock>>> fileCache) {
        val oldBlocks = fileCache.getIfPresent(sqlClassPath);
        val oldSqlIds = oldBlocks.or(new HashMap<>()).keySet();

        val parser = new EqlParser(eqlResourceLoader, sqlClassPath);
        val blocks = parser.parse(sqlContent);

        for (val block : blocks.values()) {
            val uniqueSqlId = block.getUniqueSqlId();
            sqlCache.put(uniqueSqlId, Optional.of(block));
            oldSqlIds.remove(uniqueSqlId.getSqlId());
        }

        for (val uniqueId : oldSqlIds) {
            sqlCache.invalidate(EqlUniqueSqlId.of(sqlClassPath, uniqueId));
        }

        fileCache.put(sqlClassPath, Optional.of(blocks));
    }

    public Map<String, EqlBlock> updateFileCache(
            String sqlContent,
            EqlResourceLoader eqlResourceLoader,
            String sqlClassPath, boolean eqlLazyLoad) {
        val parser = new EqlParser(eqlResourceLoader, sqlClassPath);
        return eqlLazyLoad ? parser.delayParse(sqlContent) : parser.parse(sqlContent);
    }
}
