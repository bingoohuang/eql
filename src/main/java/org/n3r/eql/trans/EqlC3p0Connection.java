package org.n3r.eql.trans;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.util.Fucks;

import java.sql.Connection;
import java.sql.SQLException;

public class EqlC3p0Connection extends AbstractEqlConnection {
    ComboPooledDataSource cpds;

    @Override
    public void initialize(EqlConfig eqlConfig) {
        cpds = new ComboPooledDataSource();
    }

    @Override
    public Connection getConnection(String dbName) {
        try {
            return cpds.getConnection();
        } catch (SQLException e) {
            throw Fucks.fuck(e);
        }
    }

    @Override
    public void destroy() {
        cpds.close();
    }

    @Override
    public String getDriverName() {
        return cpds.getDriverClass();
    }

    @Override
    public String getJdbcUrl() {
        return cpds.getJdbcUrl();
    }


}
