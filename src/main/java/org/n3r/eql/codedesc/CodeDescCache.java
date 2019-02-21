package org.n3r.eql.codedesc;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.n3r.diamond.client.Miner;
import org.n3r.eql.cache.EqlCacheKey;
import org.n3r.eql.config.EqlConfigDecorator;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.impl.EqlUniqueSqlId;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.param.EqlParamsBinder;
import org.n3r.eql.parser.EqlBlock;
import org.n3r.eql.util.EqlUtils;

@Slf4j
public class CodeDescCache {
    public static final String EQL_CACHE = "EQL.CACHE.DESC";
    static Cache<EqlUniqueSqlId, Optional<String>> cachEQLIdVersion
            = CacheBuilder.newBuilder().build();

    static Cache<EqlUniqueSqlId, Cache<EqlCacheKey, Optional<DefaultCodeDescMapper>>> cacheDict
            = CacheBuilder.newBuilder().build();

    public static DefaultCodeDescMapper getCachedMapper(
            String sqlClassPath,
            CodeDesc codeDesc,
            EqlRun currEqlRun,
            EqlConfigDecorator eqlConfig,
            EqlBlock eqlBlock,
            String tagSqlId) {

        val uniqueSQLId = new EqlUniqueSqlId(sqlClassPath, codeDesc.getDescLabel());

        val cachedSqlIdVersion = cachEQLIdVersion.getIfPresent(uniqueSQLId);
        val sqlIdVersion = getSqlIdCacheVersion(uniqueSQLId);

        val subCache = getOrCreateSubCache(uniqueSQLId);
        val eqlCacheKey = new EqlCacheKey(uniqueSQLId, codeDesc.getParams(), null, null);

        if (cachedSqlIdVersion != null && !StringUtils.equals(sqlIdVersion, cachedSqlIdVersion.orNull())) {
            subCache.invalidate(eqlCacheKey);
            cachEQLIdVersion.put(uniqueSQLId, Optional.fromNullable(sqlIdVersion));
        }

        val mapperOptional = getOrCreateMapper(currEqlRun, eqlConfig, codeDesc,
                eqlBlock, subCache, eqlCacheKey, tagSqlId);

        return mapperOptional.orNull();
    }

    private static String getSqlIdCacheVersion(EqlUniqueSqlId uniquEQLId) {
        val dataId = uniquEQLId.getSqlClassPath().replaceAll("/", ".");
        val minerable = new Miner().getMiner(EQL_CACHE, dataId);
        val key = uniquEQLId.getSqlId() + ".cacheVersion";
        return minerable.getString(key);
    }

    @SneakyThrows
    private static Optional<DefaultCodeDescMapper> getOrCreateMapper(
            final EqlRun currEqlRun,
            final EqlConfigDecorator eqlConfig,
            final CodeDesc codeDesc,
            final EqlBlock eqlBlock,
            Cache<EqlCacheKey, Optional<DefaultCodeDescMapper>> subCache,
            final EqlCacheKey eqlCacheKey,
            final String tagSqlId) {
        return subCache.get(eqlCacheKey, () -> {
            val mapper = createCodeDescMapper(eqlBlock, currEqlRun, eqlConfig, codeDesc,
                    eqlCacheKey.getUniqueSQLId().getSqlClassPath(), tagSqlId);
            return Optional.fromNullable(mapper);
        });
    }

    @SneakyThrows
    private static Cache<EqlCacheKey, Optional<DefaultCodeDescMapper>>
    getOrCreateSubCache(final EqlUniqueSqlId uniqueSQLId) {
        return cacheDict.get(uniqueSQLId, () -> {
            String sqlIdVersion = getSqlIdCacheVersion(uniqueSQLId);
            cachEQLIdVersion.put(uniqueSQLId, Optional.fromNullable(sqlIdVersion));
            return CacheBuilder.newBuilder().build();
        });
    }

    private static DefaultCodeDescMapper createCodeDescMapper(
            EqlBlock eqlBlock,
            EqlRun currEqlRun,
            EqlConfigDecorator eqlConfig,
            CodeDesc codeDesc, String sqlClassPath, String tagSqlId) {
        // try to load code desc mapping by sqlid
        try {
            val executionContext = EqlUtils.newExecContext(codeDesc.getParams(), null);
            val eqlRuns = eqlBlock.createEqlRunsByEqls(tagSqlId, eqlConfig,
                    executionContext, codeDesc.getParams(), null);
            if (eqlRuns.size() != 1)
                throw new EqlExecuteException("only one select sql supported ");

            val eqlRun = eqlRuns.get(0);
            if (!eqlRun.isLastSelectSql())
                throw new EqlExecuteException("only one select sql supported ");

            new EqlParamsBinder().prepareBindParams(eqlBlock.isIterateOption(), eqlRun);

            eqlRun.setConnection(currEqlRun.getConnection());
            @Cleanup val ps = EqlUtils.prepareSQL(sqlClassPath, eqlConfig, eqlRun,
                    codeDesc.getDescLabel(), tagSqlId);
            eqlRun.bindParams(ps, sqlClassPath);
            @Cleanup val rs = ps.executeQuery();
            rs.setFetchSize(100);

            val columnCount = rs.getMetaData().getColumnCount();
            if (columnCount < 2)
                throw new EqlExecuteException(
                        "should at least two columns used as code and desc");

            val mapper = new DefaultCodeDescMapper();
            while (rs.next()) {
                mapper.addMapping(rs.getString(1), rs.getString(2));
            }

            return mapper;
        } catch (Exception ex) {
            log.warn("error", ex);
        }

        return null;
    }
}
