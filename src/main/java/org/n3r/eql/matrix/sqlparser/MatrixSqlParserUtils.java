package org.n3r.eql.matrix.sqlparser;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.matrix.EqlMatrixConnection;
import org.n3r.eql.util.Pair;

public class MatrixSqlParserUtils {
    static LoadingCache<Pair<EqlConfig, String>, MatrixSqlParseResult> cache = CacheBuilder.newBuilder().build(
            new CacheLoader<Pair<EqlConfig, String>, MatrixSqlParseResult>() {
                @Override
                public MatrixSqlParseResult load(Pair<EqlConfig, String> key) throws Exception {
                    return new MatrixSqlParser().parse(key._1, key._2);
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
