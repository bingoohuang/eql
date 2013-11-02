package org.n3r.eql.trans;

import com.google.common.collect.Maps;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.ex.EqlConfigException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class EqlSimpleConnection implements EqlConnection {
    private SimpleDataSource dataSource = null;

    @Override
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new EqlConfigException("create connection fail", e);
        }
    }

    @Override
    public void initialize(EqlConfig eqlConfig) {
        String driver = eqlConfig.getStr("driver");
        String url = eqlConfig.getStr("url");
        String user = eqlConfig.getStr("user");
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
