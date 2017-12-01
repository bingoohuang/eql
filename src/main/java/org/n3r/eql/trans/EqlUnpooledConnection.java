package org.n3r.eql.trans;

import lombok.SneakyThrows;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.util.EqlUtils;
import org.n3r.eql.util.O;

import java.sql.Connection;
import java.util.Map;

public class EqlUnpooledConnection extends AbstractEqlConnection {
    private UnpooledDataSource dataSource = new UnpooledDataSource();

    @Override public void initialize(EqlConfig eqlConfig) {
        Map<String, String> params = eqlConfig.params();
        EqlUtils.compatibleWithUserToUsername(params);

        O.populate(dataSource, params);
    }

    @SneakyThrows
    @Override public Connection getConnection(String dbName) {
        return dataSource.getConnection();
    }

    @Override public void destroy() {

    }

    @Override public String getDriverName() {
        return dataSource.getDriver();
    }

    @Override public String getJdbcUrl() {
        return dataSource.getUrl();
    }
}
