package org.n3r.eql.trans;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.util.EqlUtils;
import org.n3r.eql.util.O;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class EqlHikariConnection extends AbstractEqlConnection {
    HikariDataSource dataSource;

    @Override
    public void initialize(EqlConfig eqlConfig) {
        Map<String, String> params = eqlConfig.params();
        EqlUtils.compatibleWithUserToUsername(params);

        HikariConfig config = new HikariConfig();
        O.populate(config, params);

        dataSource = new HikariDataSource(config);
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
        return EqlUtils.getDriverNameFromConnection(dataSource);
    }

    @Override
    public String getJdbcUrl() {
        return dataSource.getJdbcUrl();
    }
}
