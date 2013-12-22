package org.n3r.eql.matrix.sqlparser;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.tuple.Pair;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.matrix.EqlMatrixConnection;

public class MatrixSqlParserUtils {
    static LoadingCache<Pair<EqlConfig, String>, MatrixSqlParseResult> cache = CacheBuilder.newBuilder().build(
            new CacheLoader<Pair<EqlConfig, String>, MatrixSqlParseResult>() {
                @Override
                public MatrixSqlParseResult load(Pair<EqlConfig, String> key) throws Exception {
                    return new MatrixSqlParser().parse(key.getLeft(), key.getRight());
                }
            });

    public static void parse(EqlConfig eqlConfig, EqlRun eqlRun) {
        MatrixSqlParseResult result = cache.getUnchecked(Pair.of(eqlConfig, eqlRun.getRunSql()));

        if (result instanceof MatrixSqlParseNoResult) {
            EqlMatrixConnection.chooseDefaultDatabase();
            return;
        }

        EqlMatrixConnection.chooseDatabase(result.getDatabaseName(eqlRun));
    }
}
