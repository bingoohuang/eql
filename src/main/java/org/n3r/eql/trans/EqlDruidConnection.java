package org.n3r.eql.trans;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.google.common.base.Objects;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.mtcp.MtcpEnvironmentAware;
import org.n3r.eql.mtcp.utils.Mtcps;
import org.n3r.eql.util.EqlUtils;
import org.n3r.eql.util.O;

import java.sql.Connection;
import java.util.Map;

@Slf4j
public class EqlDruidConnection extends AbstractEqlConnection {
    DruidDataSource dataSource;
    MtcpEnvironmentAware mtcpEnvironmentAware;

    @Override
    public void initialize(EqlConfig eqlConfig) {
        Map<String, String> params = eqlConfig.params();
        EqlUtils.compatibleWithUserToUsername(params);

        dataSource = O.populate(new DruidDataSource(), params);
        dataSource.setInitVariants(true);
        mtcpEnvironmentAware = createMtcpEnvironmentAware(params);
    }

    private MtcpEnvironmentAware createMtcpEnvironmentAware(Map<String, String> params) {
        val implSepc = params.get("mtcpEnvironmentAwareClass.spec");
        if (StringUtils.isEmpty(implSepc)) return null;


        return Mtcps.createObjectBySpec(implSepc, MtcpEnvironmentAware.class);
    }


    @Override @SneakyThrows
    public Connection getConnection(String dbName) {
        val connection = dataSource.getConnection();

        attachMtcpEnvironment(connection);

        return connection;
    }

    @SneakyThrows
    private void attachMtcpEnvironment(DruidPooledConnection connection) {
        if (mtcpEnvironmentAware == null) return;

        String tenantId = mtcpEnvironmentAware.getTenantId();
        String name = mtcpEnvironmentAware.getClass().getName();
        Map<String, Object> variables = connection.getVariables();

        String lastTenantId = (String) variables.get(name);
        if (Objects.equal(tenantId, lastTenantId)) {
            return;
        }

        variables.put(name, tenantId);

        val tenantDatabase = mtcpEnvironmentAware.getTenantDatabase();
        if (tenantDatabase == null) return;

        connection.setCatalog(tenantDatabase);
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
