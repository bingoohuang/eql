package org.n3r.eql.codedesc;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.StringUtils;
import org.n3r.diamond.client.Miner;
import org.n3r.diamond.client.Minerable;
import org.n3r.eql.cache.EqlCacheKey;
import org.n3r.eql.config.EqlConfig;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class CodeDescCache {
    public static final String EQL_CACHE = "EQL.CACHE.DESC";
    static Cache<EqlUniqueSqlId, Optional<String>> cacheSqlIdVersion
            = CacheBuilder.newBuilder().build();

    static Cache<EqlUniqueSqlId, Cache<EqlCacheKey, Optional<DefaultCodeDescMapper>>> cacheDict
            = CacheBuilder.newBuilder().build();

    public static DefaultCodeDescMapper getCachedMapper(String sqlClassPath,
                                                        CodeDesc codeDesc,
                                                        EqlRun currEqlRun,
                                                        EqlConfigDecorator eqlConfig,
                                                        EqlBlock eqlBlock) {

        EqlUniqueSqlId uniqueSqlId = new EqlUniqueSqlId(sqlClassPath, codeDesc.getDescLabel());

        Optional<String> cachedSqlIdVersion = cacheSqlIdVersion.getIfPresent(uniqueSqlId);
        String sqlIdVersion = getSqlIdCacheVersion(uniqueSqlId);

        Cache<EqlCacheKey, Optional<DefaultCodeDescMapper>> subCache = getOrCreateSubCache(uniqueSqlId);
        EqlCacheKey eqlCacheKey = new EqlCacheKey(uniqueSqlId, codeDesc.getParams(), null, null);

        if (cachedSqlIdVersion != null && !StringUtils.equals(sqlIdVersion, cachedSqlIdVersion.orNull())) {
            subCache.invalidate(eqlCacheKey);
            cacheSqlIdVersion.put(uniqueSqlId, Optional.fromNullable(sqlIdVersion));
        }

        Optional<DefaultCodeDescMapper> mapperOptional = getOrCreateMapper(currEqlRun, eqlConfig, codeDesc,
                eqlBlock, subCache, eqlCacheKey);

        return mapperOptional.orNull();
    }

    private static String getSqlIdCacheVersion(EqlUniqueSqlId uniqueSqlId) {
        final String dataId = uniqueSqlId.getSqlClassPath().replaceAll("/", ".");
        Minerable minerable = new Miner().getMiner(EQL_CACHE, dataId);
        String key = uniqueSqlId.getSqlId() + ".cacheVersion";
        return minerable.getString(key);
    }

    private static Optional<DefaultCodeDescMapper> getOrCreateMapper(final EqlRun currEqlRun,
                                                                     final EqlConfigDecorator eqlConfig,
                                                                     final CodeDesc codeDesc,
                                                                     final EqlBlock eqlBlock,
                                                                     Cache<EqlCacheKey, Optional<DefaultCodeDescMapper>> subCache,
                                                                     EqlCacheKey eqlCacheKey) {
        try {
            return subCache.get(eqlCacheKey, new Callable<Optional<DefaultCodeDescMapper>>() {
                @Override
                public Optional<DefaultCodeDescMapper> call() throws Exception {
                    DefaultCodeDescMapper mapper = createCodeDescMapper(eqlBlock, currEqlRun, eqlConfig, codeDesc);
                    return Optional.fromNullable(mapper);
                }
            });
        } catch (ExecutionException e) {
            // should now happen
            throw Throwables.propagate(e);
        }
    }

    private static Cache<EqlCacheKey, Optional<DefaultCodeDescMapper>> getOrCreateSubCache(final EqlUniqueSqlId uniqueSqlId) {
        try {
            return cacheDict.get(uniqueSqlId, new Callable<Cache<EqlCacheKey, Optional<DefaultCodeDescMapper>>>() {
                @Override
                public Cache<EqlCacheKey, Optional<DefaultCodeDescMapper>> call() throws Exception {
                    String sqlIdVersion = getSqlIdCacheVersion(uniqueSqlId);
                    cacheSqlIdVersion.put(uniqueSqlId, Optional.fromNullable(sqlIdVersion));
                    return CacheBuilder.newBuilder().build();
                }
            });
        } catch (ExecutionException e) {
            // should now happen
            throw Throwables.propagate(e);
        }
    }


    private static DefaultCodeDescMapper createCodeDescMapper(EqlBlock eqlBlock,
                                                              EqlRun currEqlRun,
                                                              EqlConfigDecorator eqlConfig,
                                                              CodeDesc codeDesc) {
        // try to load code desc mapping by sqlid
        try {
            Map<String, Object> executionContext = EqlUtils.newExecContext(codeDesc.getParams(), null);
            List<EqlRun> eqlRuns = eqlBlock.createEqlRunsByEqls(eqlConfig, executionContext, codeDesc.getParams(), null);
            if (eqlRuns.size() != 1) throw new EqlExecuteException("only one select sql supported ");

            EqlRun eqlRun = eqlRuns.get(0);
            if (!eqlRun.isLastSelectSql()) throw new EqlExecuteException("only one select sql supported ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                new EqlParamsBinder().prepareBindParams(eqlBlock.hasIterateOption(), eqlRun);

                eqlRun.setConnection(currEqlRun.getConnection());
                ps = EqlUtils.prepareSql(eqlConfig, eqlRun, codeDesc.getDescLabel());
                eqlRun.bindParams(ps);
                rs = ps.executeQuery();
                rs.setFetchSize(100);

                int columnCount = rs.getMetaData().getColumnCount();
                if (columnCount < 2) throw new EqlExecuteException("should at least two columns used as code and desc");

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
