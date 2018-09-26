package org.n3r.eql.trans;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import lombok.val;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.util.EqlUtils;
import org.n3r.eql.util.O;

import java.sql.Connection;

public class EqlHikariConnection extends AbstractEqlConnection {
    HikariDataSource dataSource;

    @Override
    public void initialize(EqlConfig eqlConfig) {
        val params = eqlConfig.params();
        EqlUtils.compatibleWithUserToUsername(params);

        val config = new HikariConfig();
        O.populate(config, params);

        dataSource = new HikariDataSource(config);
    }

    @Override @SneakyThrows
    public Connection getConnection(String dbName) {
        return dataSource.getConnection();
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
