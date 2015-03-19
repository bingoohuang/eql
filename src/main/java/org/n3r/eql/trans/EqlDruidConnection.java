package org.n3r.eql.trans;

import com.alibaba.druid.pool.DruidDataSource;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.util.O;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class EqlDruidConnection extends AbstractEqlConnection {
    DruidDataSource dataSource;

    @Override
    public void initialize(EqlConfig eqlConfig) {
        Map<String, String> params = eqlConfig.params();
        compatibleWithUserToUsername(params);

        dataSource = O.populate(new DruidDataSource(), params);
    }

    private void compatibleWithUserToUsername(Map<String, String> params) {
        if (params.containsKey("username")) return;
        if (params.containsKey("user")) params.put("username", params.get("user"));
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
