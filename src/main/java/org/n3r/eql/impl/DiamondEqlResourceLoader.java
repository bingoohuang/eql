package org.n3r.eql.impl;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.n3r.diamond.client.DiamondListener;
import org.n3r.diamond.client.DiamondManager;
import org.n3r.diamond.client.DiamondStone;
import org.n3r.eql.base.EqlResourceLoader;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.parser.EqlBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import static org.n3r.eql.impl.FileEqlResourceLoader.updateBlockCache;

public class DiamondEqlResourceLoader implements EqlResourceLoader {
    static Logger log = LoggerFactory.getLogger(FileEqlResourceLoader.class);
    static Cache<String, Optional<Map<String, EqlBlock>>> fileCache = CacheBuilder.newBuilder().build();
    static Cache<String, EqlBlock> sqlCache = CacheBuilder.newBuilder().build();
    static FileEqlResourceLoader fileLoader = new FileEqlResourceLoader();

    @Override
    public void initialize(EqlConfig eqlConfig) {
    }

    @Override
    public EqlBlock loadEqlBlock(String sqlClassPath, String sqlId) {
        load(this, sqlClassPath);

        EqlBlock eqlBlock = sqlCache.getIfPresent(cacheKey(sqlClassPath, sqlId));
        if (eqlBlock == null) eqlBlock = fileLoader.loadEqlBlock(sqlClassPath, sqlId);
        if (eqlBlock != null) return eqlBlock;

        throw new RuntimeException("unable to find sql id " + sqlId);
    }

    @Override
    public Map<String, EqlBlock> load(String classPath) {
        return load(this, classPath);
    }

    public static Map<String, EqlBlock> load(final EqlResourceLoader eqlResourceLoader, final String sqlClassPath) {
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
                        updateBlockCache(diamondStone.getContent(), eqlResourceLoader, sqlClassPath);
                    }
                });

                if (sqlContent == null) {
                    log.warn("classpath sql {} not found", dataId);
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

    private static String cacheKey(String sqlClassPath, String sqlId) {
        return sqlClassPath + ":" + sqlId;
    }
}
