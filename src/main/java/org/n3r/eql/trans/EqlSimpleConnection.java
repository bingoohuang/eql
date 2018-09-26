package org.n3r.eql.trans;

import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.util.S;

import java.sql.Connection;
import java.util.Map;

public class EqlSimpleConnection extends AbstractEqlConnection {
    private SimpleDataSource dataSource;

    @Override @SneakyThrows
    public Connection getConnection(String dbName) {
        return dataSource.getConnection();
    }

    @Override
    public void destroy() {
        dataSource.forceCloseAll();
    }

    @Override
    public String getDriverName() {
        return dataSource.getJdbcDriver();
    }

    @Override
    public String getJdbcUrl() {
        return dataSource.getJdbcUrl();
    }

    @Override
    public void initialize(EqlConfig eqlConfig) {
        String driver = eqlConfig.getStr("driver");
        String url = eqlConfig.getStr("url");

        String user = eqlConfig.getStr("username");
        if (S.isBlank(user)) user = eqlConfig.getStr("user");

        String pass = eqlConfig.getStr("password");

        loadDataSource(driver, url, user, pass);
    }

    private void loadDataSource(String driver, String url, String user, String pass) {
        Map<String, String> properties = Maps.newHashMap();
        properties.put(SimpleDataSource.PROP_JDBC_DRIVER, driver);
        properties.put(SimpleDataSource.PROP_JDBC_URL, url);
        properties.put(SimpleDataSource.PROP_JDBC_USERNAME, user);
        properties.put(SimpleDataSource.PROP_JDBC_PASSWORD, pass);

        dataSource = new SimpleDataSource(properties);
    }

}
