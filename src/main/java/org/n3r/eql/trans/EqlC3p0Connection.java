package org.n3r.eql.trans;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import lombok.SneakyThrows;
import org.n3r.eql.config.EqlConfig;

import java.sql.Connection;

public class EqlC3p0Connection extends AbstractEqlConnection {
    ComboPooledDataSource cpds;

    @Override
    public void initialize(EqlConfig eqlConfig) {
        cpds = new ComboPooledDataSource();
    }

    @Override @SneakyThrows
    public Connection getConnection(String dbName) {
        return cpds.getConnection();
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
