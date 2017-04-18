package org.n3r.eql.diamond;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.n3r.diamond.client.DiamondListenerAdapter;
import org.n3r.diamond.client.DiamondManager;
import org.n3r.diamond.client.DiamondStone;
import org.n3r.eql.base.EqlResourceLoader;
import org.n3r.eql.impl.AbstractEqlResourceLoader;
import org.n3r.eql.impl.EqlResourceLoaderHelper;
import org.n3r.eql.impl.EqlUniqueSqlId;
import org.n3r.eql.impl.FileEqlResourceLoader;
import org.n3r.eql.parser.EqlBlock;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static org.n3r.eql.impl.EqlResourceLoaderHelper.updateBlockCache;
import static org.n3r.eql.impl.EqlResourceLoaderHelper.updateFileCache;

@Slf4j
public class DiamondEqlResourceLoader extends AbstractEqlResourceLoader {
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

        val key = new EqlUniqueSqlId(sqlClassPath, sqlId);
        val blockOptional = sqlCache.getUnchecked(key);
        if (blockOptional.isPresent()) return blockOptional.get();

        val eqlBlock = fileLoader.loadEqlBlock(sqlClassPath, sqlId);
        if (eqlBlock != null) return eqlBlock;

        throw new RuntimeException("unable to find sql id " + sqlId);
    }

    @Override
    public Map<String, EqlBlock> load(String classPath) {
        return load(this, classPath);
    }

    @SneakyThrows
    private Map<String, EqlBlock> load(
            final EqlResourceLoader eqlResourceLoader,
            final String sqlClassPath) {
        val dataId = sqlClassPath.replaceAll("/", ".");
        val valueLoader = new Callable<Optional<Map<String, EqlBlock>>>() {
            @Override
            public Optional<Map<String, EqlBlock>> call() throws Exception {
                val manager = new DiamondManager("EQL", dataId);
                manager.addDiamondListener(new DiamondListenerAdapter() {
                    @Override
                    public void accept(DiamondStone diamondStone) {
                        String eql = diamondStone.getContent();
                        updateBlockCache(eql, eqlResourceLoader, sqlClassPath, sqlCache, fileCache);
                    }
                });

                val sqlContent = manager.getDiamond();
                if (sqlContent == null) {
                    log.warn("classpath sql {} not found", dataId);
                    return Optional.absent();
                }

                return Optional.of(updateFileCache(sqlContent,
                        eqlResourceLoader, sqlClassPath, eqlLazyLoad));
            }
        };

        try {
            return fileCache.get(sqlClassPath, valueLoader).orNull();
        } catch (ExecutionException e) {
            throw Throwables.getRootCause(e);
        }
    }

}
