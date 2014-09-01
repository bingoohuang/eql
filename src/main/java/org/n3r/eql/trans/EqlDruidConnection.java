package org.n3r.eql.trans;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.lang3.StringUtils;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.ex.EqlExecuteException;

import java.sql.Connection;
import java.sql.SQLException;

public class EqlDruidConnection implements EqlConnection {
    DruidDataSource dataSource;

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

        dataSource = new DruidDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        int myInitialSize = parseIntParameter(initialSize);
        if (myInitialSize > 0) dataSource.setInitialSize(myInitialSize);

        int myMinIdle = parseIntParameter(minIdle);
        if (myMinIdle > 0) dataSource.setMinIdle(myMinIdle);

        int myMaxActive = parseIntParameter(maxActive);
        if (myMaxActive > 0) dataSource.setMaxActive(myMaxActive);

        int myMaxWait = parseIntParameter(maxWait);
        if (myMaxWait > 0) dataSource.setMaxWait(myMaxWait);

        int myTimeBetweenEvictionRunsMillis = parseIntParameter(timeBetweenEvictionRunsMillis);
        if (myTimeBetweenEvictionRunsMillis > 0)
            dataSource.setTimeBetweenEvictionRunsMillis(myTimeBetweenEvictionRunsMillis);

        int myMinEvictableIdleTimeMillis = parseIntParameter(minEvictableIdleTimeMillis);
        if (myMinEvictableIdleTimeMillis > 0)
            dataSource.setMinEvictableIdleTimeMillis(myMinEvictableIdleTimeMillis);

        if (StringUtils.isNotBlank(validationQuery))
            dataSource.setValidationQuery(validationQuery);

    }

    private int parseIntParameter(String param) {
        return param.matches("\\d+") ? Integer.parseInt(param) : 0;
    }

    @Override
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new EqlExecuteException(e);
        }
    }

    @Override
    public void destroy() {
        dataSource.close();
    }
}
