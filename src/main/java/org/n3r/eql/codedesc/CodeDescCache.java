package org.n3r.eql.codedesc;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.n3r.diamond.client.Miner;
import org.n3r.diamond.client.Minerable;
import org.n3r.eql.cache.EqlCacheKey;
import org.n3r.eql.config.EqlConfigDecorator;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.impl.EqlUniqueSqlId;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.param.EqlParamsBinder;
import org.n3r.eql.parser.EqlBlock;
import org.n3r.eql.util.Closes;
import org.n3r.eql.util.EqlUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Callable;

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
        String sqlIdVersion = getSqlIdCacheVersion(uniqueSQLId);

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
        final String dataId = uniquEQLId.getSqlClassPath().replaceAll("/", ".");
        Minerable minerable = new Miner().getMiner(EQL_CACHE, dataId);
        String key = uniquEQLId.getSqlId() + ".cacheVersion";
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
        return subCache.get(eqlCacheKey, new Callable<Optional<DefaultCodeDescMapper>>() {
            @Override
            public Optional<DefaultCodeDescMapper> call() throws Exception {
                val mapper = createCodeDescMapper(eqlBlock, currEqlRun, eqlConfig, codeDesc,
                        eqlCacheKey.getUniqueSQLId().getSqlClassPath(), tagSqlId);
                return Optional.fromNullable(mapper);
            }
        });
    }

    @SneakyThrows
    private static Cache<EqlCacheKey, Optional<DefaultCodeDescMapper>>
    getOrCreateSubCache(final EqlUniqueSqlId uniqueSQLId) {
        return cacheDict.get(uniqueSQLId, new Callable<Cache<EqlCacheKey, Optional<DefaultCodeDescMapper>>>() {
            @Override
            public Cache<EqlCacheKey, Optional<DefaultCodeDescMapper>> call() throws Exception {
                String sqlIdVersion = getSqlIdCacheVersion(uniqueSQLId);
                cachEQLIdVersion.put(uniqueSQLId, Optional.fromNullable(sqlIdVersion));
                return CacheBuilder.newBuilder().build();
            }
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

            EqlRun eqlRun = eqlRuns.get(0);
            if (!eqlRun.isLastSelectSql())
                throw new EqlExecuteException("only one select sql supported ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                new EqlParamsBinder().prepareBindParams(eqlBlock.isIterateOption(), eqlRun);

                eqlRun.setConnection(currEqlRun.getConnection());
                ps = EqlUtils.prepareSQL(sqlClassPath, eqlConfig, eqlRun,
                        codeDesc.getDescLabel(), tagSqlId);
                eqlRun.bindParams(ps, sqlClassPath);
                rs = ps.executeQuery();
                rs.setFetchSize(100);

                int columnCount = rs.getMetaData().getColumnCount();
                if (columnCount < 2)
                    throw new EqlExecuteException(
                            "should at least two columns used as code and desc");

                DefaultCodeDescMapper mapper = new DefaultCodeDescMapper();
                while (rs.next()) {
                    mapper.addMapping(rs.getString(1), rs.getString(2));
                }


                return mapper;
            } finally {
                Closes.closeQuietly(rs, ps);
            }


        } catch (Exception ex) {
            ex.printStackTrace();
            // ignore
        }

        return null;
    }
}
