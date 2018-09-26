package org.n3r.eql.matrix;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.matrix.sqlparser.MatrixSqlParseNoResult;
import org.n3r.eql.matrix.sqlparser.MatrixSqlParseResult;
import org.n3r.eql.matrix.sqlparser.MatrixSqlParser;
import org.n3r.eql.trans.EqlConnection;
import org.n3r.eql.util.O;
import org.n3r.eql.util.Pair;
import org.n3r.eql.util.PropertyValueFilter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Slf4j
public class EqlMatrixConnection implements EqlConnection {
    public static final String DEFAULT = "default";
    LoadingCache<String, DruidDataSource> dataSourceCache;
    private String url;


    @Override
    public void initialize(EqlConfig eqlConfig) {
        this.url = eqlConfig.getStr("url");
        val params = eqlConfig.params();

        dataSourceCache = CacheBuilder.newBuilder().build(new CacheLoader<String, DruidDataSource>() {
            @Override
            public DruidDataSource load(final String database) {
                return O.populate(new DruidDataSource(), params, new PropertyValueFilter() {
                    @Override
                    public String filter(String propertyValue) {
                        return parseParameter(propertyValue, database);
                    }
                });
            }
        });
    }

    LoadingCache<Pair<EqlConfig, String>, MatrixSqlParseResult> cache = CacheBuilder.newBuilder().build(
            new CacheLoader<Pair<EqlConfig, String>, MatrixSqlParseResult>() {
                @Override
                public MatrixSqlParseResult load(Pair<EqlConfig, String> key) {
                    return new MatrixSqlParser().parse(key._1, key._2);
                }
            });

    static ThreadLocal<String> dbNameTL = new ThreadLocal<String>();

    public static void chooseDbName(String dbName) {
        dbNameTL.set(dbName);
    }

    @Override
    public String getDbName(EqlConfig eqlConfig, EqlRun eqlRun) {
        val result = cache.getUnchecked(Pair.of(eqlConfig, eqlRun.getRunSql()));

        if (result instanceof MatrixSqlParseNoResult) return DEFAULT;

        return result.getDatabaseName(eqlRun);
    }

    @Override
    public Connection getConnection(String dbName) {
        String localDbName;

        if (!DEFAULT.equals(dbName)) localDbName = dbName;
        else localDbName = dbNameTL.get() != null ? dbNameTL.get() : DEFAULT;

        try {
            val dataSource = dataSourceCache.getUnchecked(localDbName);
            log.debug("use database [{}]", localDbName);
            return dataSource.getConnection();

        } catch (SQLException e) {
            throw new EqlExecuteException("unable to find database " + localDbName, e);
        }
    }

    private static String parseParameter(String param, String database) {
        if (param == null || param.length() == 0) return "";

        StringBuilder parsed = new StringBuilder();

        val splitter = Splitter.on(',').trimResults()
                .omitEmptyStrings().withKeyValueSeparator("->");
        int startPos = 0;
        while (startPos < param.length()) {
            int leftBracePos = param.indexOf('{', startPos);
            if (leftBracePos < 0) {
                parsed.append(param.substring(startPos));
                break;
            } else if (leftBracePos > 0) {
                parsed.append(param, startPos, leftBracePos);
            }

            int rightBracePos = param.indexOf('}', leftBracePos);
            if (rightBracePos < 0) {
                log.warn("invalid parameter format: " + param);
                return param;
            }

            String map = param.substring(leftBracePos + 1, rightBracePos);
            Map<String, String> data = splitter.split(map);
            String specified = data.get(database);
            if (specified == null) specified = data.get(DEFAULT);
            if (specified == null) {
                log.warn("invalid parameter mapping format: " + param);
                return param;
            }
            parsed.append(specified);
            startPos = rightBracePos + 1;
        }

        return parsed.toString();
    }

    @Override
    public void destroy() {
        val map = dataSourceCache.asMap();

        for (String databaseName : map.keySet()) {
            val druidDataSource = map.get(databaseName);
            try {
                druidDataSource.close();
            } catch (Exception e) {
                // ignore
            }
        }

        dataSourceCache.invalidateAll();
        dataSourceCache = null;
    }

    @Override
    public String getDriverName() {
        return url;
    }

    @Override
    public String getJdbcUrl() {
        return url;
    }
}
