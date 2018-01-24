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
        val loader = new CacheLoader<EqlUniqueSqlId, Optional<EqlBlock>>() {
            @Override
            public Optional<EqlBlock> load(EqlUniqueSqlId eqlUniqueSqlId) throws Exception {
                return loadEqlBlockOptional(eqlUniqueSqlId, fileCache);
            }
        };
        return CacheBuilder.newBuilder().build(loader);
    }

    private Optional<EqlBlock> loadEqlBlockOptional(
            EqlUniqueSqlId eqlUniqueSqlId,
            Cache<String, Optional<Map<String, EqlBlock>>> fileCache) {
        val optional = loadBlocks(fileCache, eqlUniqueSqlId);
        if (!optional.isPresent()) {
            log.error("unable to find sqlid:{}", eqlUniqueSqlId);
        }

        return optional;
    }

    private Optional<EqlBlock> loadBlocks(
            Cache<String, Optional<Map<String, EqlBlock>>> fileCache,
            EqlUniqueSqlId eqlUniqueSqlId) {
        val blocks = fileCache.getIfPresent(eqlUniqueSqlId.getSqlClassPath());
        if (/*blocks == null || */!blocks.isPresent()) return Optional.absent();

        val eqlBlock = blocks.get().get(eqlUniqueSqlId.getSqlId());
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
        val oldSqlIds = oldBlocks.or(new HashMap<String, EqlBlock>()).keySet();

        val eqlParser = new EqlParser(eqlResourceLoader, sqlClassPath);
        val sqlBlocks = eqlParser.parse(sqlContent);

        for (val sqlBlock : sqlBlocks.values()) {
            val uniqueSqlId = sqlBlock.getUniqueSqlId();
            sqlCache.put(uniqueSqlId, Optional.of(sqlBlock));
            oldSqlIds.remove(uniqueSqlId.getSqlId());
        }

        for (val uniqueId : oldSqlIds) {
            sqlCache.invalidate(new EqlUniqueSqlId(sqlClassPath, uniqueId));
        }

        fileCache.put(sqlClassPath, Optional.of(sqlBlocks));
    }

    public Map<String, EqlBlock> updateFileCache(
            String sqlContent,
            EqlResourceLoader eqlResourceLoader,
            String sqlClassPath, boolean eqlLazyLoad) {
        val eqlParser = new EqlParser(eqlResourceLoader, sqlClassPath);
        return eqlLazyLoad
                ? eqlParser.delayParse(sqlContent)
                : eqlParser.parse(sqlContent);
    }
}
