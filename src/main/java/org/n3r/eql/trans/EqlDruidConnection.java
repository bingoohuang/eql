package org.n3r.eql.trans;

import com.alibaba.druid.pool.DruidDataSource;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.util.O;

import java.sql.Connection;
import java.sql.SQLException;

public class EqlDruidConnection extends AbstractEqlConnection {
    DruidDataSource dataSource;

    @Override
    public void initialize(EqlConfig eqlConfig) {
        dataSource = O.populate(new DruidDataSource(), eqlConfig.params());
    }

    @Override
    public Connection getConnection(String dbName) {
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

    @Override
    public String getDriverName() {
        return dataSource.getDriverClassName();
    }
}
