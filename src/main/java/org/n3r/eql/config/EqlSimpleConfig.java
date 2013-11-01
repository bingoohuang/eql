package org.n3r.eql.config;

import com.google.common.base.Throwables;
import org.n3r.eql.EqlTran;
import org.n3r.eql.ex.EqlConfigException;
import org.n3r.eql.trans.EqlJdbcTransaction;
import org.n3r.eql.trans.EqlJtaTransaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class EqlSimpleConfig implements EqlTranAware {
    private String url;
    private String user;
    private String pass;
    private String driver;
    private String transactionType;
    private SimpleDataSource dataSource = null;
    private String databaseId;

    private Connection getConnection() {
        try {
            if (dataSource == null) loadDataSource();

            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new EqlConfigException("create connection fail", e);
        }
    }

    private void loadDataSource() {
        try {
            Properties properties = new Properties();
            properties.put(SimpleDataSource.PROP_JDBC_DRIVER, driver);
            properties.put(SimpleDataSource.PROP_JDBC_URL, url);
            properties.put(SimpleDataSource.PROP_JDBC_USERNAME, user);
            properties.put(SimpleDataSource.PROP_JDBC_PASSWORD, pass);

            dataSource = new SimpleDataSource(properties);

        } catch (Exception ex) {
            throw Throwables.propagate(ex);
        }
    }

    @Override
    public EqlTran getTran() {
        if ("jta".equals(transactionType)) return new EqlJtaTransaction();

        return new EqlJdbcTransaction(getConnection());
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public void setUser(String user) {
        this.user = user;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

}
