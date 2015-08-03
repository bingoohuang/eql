package org.n3r.eql.mtcp.impl;

import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.mtcp.MtcpDataSourceHandler;
import org.n3r.eql.trans.AbstractEqlConnection;
import org.n3r.eql.util.Fucks;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class MtcpEqlConnection extends AbstractEqlConnection {
    DataSource dataSource;
    String driverClassName;

    @Override
    public void initialize(EqlConfig eqlConfig) {
        driverClassName = eqlConfig.getStr("driverClassName");
        dataSource = new MtcpDataSourceHandler(eqlConfig).newMtcpDataSource();
    }

    @Override
    public Connection getConnection(String dbName) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw Fucks.fuck(e);
        }
    }

    @Override
    public void destroy() {
        if (dataSource instanceof Closeable) {
            try {
                ((Closeable) dataSource).close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    @Override
    public String getDriverName() {
        return driverClassName; // dataSource.getDriverClassName();
    }

    @Override
    public String getJdbcUrl() {
        return null; //dataSource.getUrl();
    }
}
