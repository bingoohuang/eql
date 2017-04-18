package org.n3r.eql.trans;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.SneakyThrows;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.util.EqlUtils;
import org.n3r.eql.util.O;

import java.sql.Connection;
import java.util.Map;

public class EqlDruidConnection extends AbstractEqlConnection {
    DruidDataSource dataSource;

    @Override
    public void initialize(EqlConfig eqlConfig) {
        Map<String, String> params = eqlConfig.params();
        EqlUtils.compatibleWithUserToUsername(params);

        dataSource = O.populate(new DruidDataSource(), params);
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
        return dataSource.getDriverClassName();
    }

    @Override
    public String getJdbcUrl() {
        return dataSource.getUrl();
    }
}
