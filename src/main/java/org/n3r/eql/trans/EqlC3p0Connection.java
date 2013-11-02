package org.n3r.eql.trans;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.ex.EqlExecuteException;

import java.sql.Connection;
import java.sql.SQLException;

public class EqlC3p0Connection implements EqlConnection {
    private ComboPooledDataSource cpds = new ComboPooledDataSource();

    @Override
    public void initialize(EqlConfig eqlConfig) {
    }

    @Override
    public Connection getConnection() {
        try {
            return cpds.getConnection();
        } catch (SQLException e) {
            throw new EqlExecuteException(e);
        }
    }

}
