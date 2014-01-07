package org.n3r.eql.matrix;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.trans.EqlConnection;
import org.n3r.eql.util.EqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class EqlMatrixConnection implements EqlConnection {
    public static final String DEFAULT = "default";
    LoadingCache<String, DruidDataSource> dataSourceCache;
    static ThreadLocal<String> databaseNameTl = new ThreadLocal<String>();
    Logger logger = LoggerFactory.getLogger(EqlMatrixConnection.class);

    public static void chooseDatabase(String databaseName) {
        databaseNameTl.set(databaseName);
    }


    public static void chooseDefaultDatabase() {
        databaseNameTl.set(DEFAULT);
    }

    @Override
    public void initialize(EqlConfig eqlConfig) {
        final String url = eqlConfig.getStr("url");
        final String username = eqlConfig.getStr("username");
        final String password = eqlConfig.getStr("password");

        final String initialSize = eqlConfig.getStr("initialSize");
        final String minIdle = eqlConfig.getStr("minIdle");
        final String maxActive = eqlConfig.getStr("maxActive");
        final String maxWait = eqlConfig.getStr("maxWait");
        final String timeBetweenEvictionRunsMillis = eqlConfig.getStr("timeBetweenEvictionRunsMillis");
        final String minEvictableIdleTimeMillis = eqlConfig.getStr("minEvictableIdleTimeMillis");
        final String validationQuery = eqlConfig.getStr("validationQuery");

        dataSourceCache = CacheBuilder.newBuilder().build(new CacheLoader<String, DruidDataSource>() {
            @Override
            public DruidDataSource load(String database) throws Exception {
                return createDruidDataSource(database, url, username, password,
                        initialSize, minIdle, maxActive, maxWait,
                        timeBetweenEvictionRunsMillis, minEvictableIdleTimeMillis, validationQuery);
            }
        });
    }

    private DruidDataSource createDruidDataSource(String database, String url, String username, String password,
                                                  String initialSize, String minIdle, String maxActive, String maxWait,
                                                  String timeBetweenEvictionRunsMillis,
                                                  String minEvictableIdleTimeMillis,
                                                  String validationQuery) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(parseParameter(url, database));
        dataSource.setUsername(parseParameter(username, database));
        dataSource.setPassword(parseParameter(password, database));

        int myInitialSize = parseIntParameter(initialSize, database);
        if (myInitialSize > 0) dataSource.setInitialSize(myInitialSize);

        int myMinIdle = parseIntParameter(minIdle, database);
        if (myMinIdle > 0) dataSource.setMinIdle(myMinIdle);

        int myMaxActive = parseIntParameter(maxActive, database);
        if (myMaxActive > 0) dataSource.setMaxActive(myMaxActive);

        int myMaxWait = parseIntParameter(maxWait, database);
        if (myMaxWait > 0) dataSource.setMaxWait(myMaxWait);

        int myTimeBetweenEvictionRunsMillis = parseIntParameter(timeBetweenEvictionRunsMillis, database);
        if (myTimeBetweenEvictionRunsMillis > 0)
            dataSource.setTimeBetweenEvictionRunsMillis(myTimeBetweenEvictionRunsMillis);

        int myMinEvictableIdleTimeMillis = parseIntParameter(minEvictableIdleTimeMillis, database);
        if (myMinEvictableIdleTimeMillis > 0)
            dataSource.setMinEvictableIdleTimeMillis(myMinEvictableIdleTimeMillis);

        String myValidationQuery = parseParameter(validationQuery, database);
        if (myValidationQuery.length() > 0) dataSource.setValidationQuery(myValidationQuery);

        return dataSource;
    }

    private int parseIntParameter(String param, String database) {
        String my = parseParameter(param, database);

        return my.matches("\\d+") ? Integer.parseInt(my) : 0;
    }

    private String parseParameter(String param, String database) {
        if (param == null || param.length() == 0) return "";

        StringBuilder parsed = new StringBuilder();

        Splitter.MapSplitter splitter = Splitter.on(',').trimResults()
                .omitEmptyStrings().withKeyValueSeparator("->");
        int startPos = 0;
        while (startPos < param.length()) {
            int leftBracePos = param.indexOf('{', startPos);
            if (leftBracePos < 0) {
                parsed.append(param.substring(startPos));
                break;
            } else if (leftBracePos > 0) {
                parsed.append(param.substring(startPos, leftBracePos));
            }

            int rightBracePos = param.indexOf('}', leftBracePos);
            if (rightBracePos < 0) {
                logger.warn("invalid parameter format: " + param);
                return param;
            }

            String map = param.substring(leftBracePos + 1, rightBracePos);
            Map<String, String> data = splitter.split(map);
            String specified = data.get(database);
            if (specified == null) specified = data.get(DEFAULT);
            if (specified == null) {
                logger.warn("invalid parameter mapping format: " + param);
                return param;
            }
            parsed.append(specified);
            startPos = rightBracePos + 1;
        }

        return parsed.toString();
    }

    @Override
    public Connection getConnection() {
        try {
            String databaseName = databaseNameTl.get();
            if (EqlUtils.isBlank(databaseName)) databaseName = DEFAULT;
            logger.debug("use database [{}]", databaseName);
            return dataSourceCache.getUnchecked(databaseName).getConnection();
        } catch (SQLException e) {
            throw new EqlExecuteException(e);
        }
    }

}
