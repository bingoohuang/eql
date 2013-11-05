package org.n3r.eql.impl;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.n3r.diamond.client.DiamondListener;
import org.n3r.diamond.client.DiamondManager;
import org.n3r.diamond.client.DiamondStone;
import org.n3r.eql.base.EqlResourceLoader;
import org.n3r.eql.parser.EqlBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import static org.n3r.eql.impl.EqlResourceLoaderHelper.updateBlockCache;
import static org.n3r.eql.impl.EqlResourceLoaderHelper.updateFileCache;

public class DiamondEqlResourceLoader extends AbstractEqlResourceLoader {
    static Logger log = LoggerFactory.getLogger(FileEqlResourceLoader.class);
    static Cache<String, Optional<Map<String, EqlBlock>>> fileCache;
    static LoadingCache<EqlUniqueSqlId, Optional<EqlBlock>> sqlCache;
    static FileEqlResourceLoader fileLoader = new FileEqlResourceLoader();

    static {
        fileCache = CacheBuilder.newBuilder().build();
        sqlCache = EqlResourceLoaderHelper.buildSqlCache(fileCache);
    }

    @Override
    public EqlBlock loadEqlBlock(String sqlClassPath, String sqlId) {
        load(this, sqlClassPath);

        Optional<EqlBlock> blockOptional = sqlCache.getUnchecked(new EqlUniqueSqlId(sqlClassPath, sqlId));
        if (blockOptional.isPresent()) return blockOptional.get();

        EqlBlock eqlBlock = fileLoader.loadEqlBlock(sqlClassPath, sqlId);
        if (eqlBlock != null) return eqlBlock;

        throw new RuntimeException("unable to find sql id " + sqlId);
    }

    @Override
    public Map<String, EqlBlock> load(String classPath) {
        return load(this, classPath);
    }

    private Map<String, EqlBlock> load(final EqlResourceLoader eqlResourceLoader, final String sqlClassPath) {
        final String dataId = sqlClassPath.replaceAll("/", ".");
        Callable<Optional<Map<String, EqlBlock>>> valueLoader = new Callable<Optional<Map<String, EqlBlock>>>() {
            @Override
            public Optional<Map<String, EqlBlock>> call() throws Exception {
                DiamondManager diamondManager = new DiamondManager("EQL", dataId);
                String sqlContent = diamondManager.getDiamond();
                diamondManager.addDiamondListener(new DiamondListener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void accept(DiamondStone diamondStone) {
                        String eql = diamondStone.getContent();
                        updateBlockCache(eql, eqlResourceLoader, sqlClassPath, sqlCache, fileCache);
                    }
                });

                if (sqlContent == null) {
                    log.warn("classpath sql {} not found", dataId);
                    return Optional.absent();
                }

                return Optional.of(updateFileCache(sqlContent, eqlResourceLoader, sqlClassPath));
            }
        };

        try {
            return fileCache.get(sqlClassPath, valueLoader).orNull();
        } catch (ExecutionException e) {
            Throwables.propagateIfPossible(Throwables.getRootCause(e));
        }
        return null;
    }

}
