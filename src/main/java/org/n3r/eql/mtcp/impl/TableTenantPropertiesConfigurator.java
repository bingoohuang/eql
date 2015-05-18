package org.n3r.eql.mtcp.impl;

import com.google.common.collect.Maps;
import org.n3r.eql.Eql;
import org.n3r.eql.diamond.Dql;
import org.n3r.eql.map.EqlRowMapper;
import org.n3r.eql.mtcp.TenantPropertiesConfigurator;
import org.n3r.eql.mtcp.utils.Mtcps;
import org.n3r.eql.spec.ParamsAppliable;
import org.n3r.eql.util.Rs;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

public class TableTenantPropertiesConfigurator implements TenantPropertiesConfigurator, ParamsAppliable {
    static String urlTemplate
            = "jdbc:mysql://{host}:{port}/{dbname}?[useUnicode,characterEncoding,connectTimeout,autoReconnect]";
    Map<String, Map<String, String>> tenantPropertiesMap = Maps.newHashMap();

    @Override
    public Map<String, String> getTenantProperties(String tenantId) {
        Map<String, String> map = tenantPropertiesMap.get(tenantId);
        if (map == null) throw new RuntimeException(tenantId + "'s config is not found");

        return map;
    }

    @Override
    public void applyParams(String[] params) {
        String eqlConfigName = params[0];
        String tenantPropsTable = params[1];
        boolean dql = params.length >= 3 && "Dql".equalsIgnoreCase(params[2]);

        Eql eql = dql ? new Dql(eqlConfigName) : new Eql(eqlConfigName);
        eql.returnType(new EqlRowMapper() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum, boolean isSingleColumn) throws SQLException {
                Map<String, String> tenantProperties = Maps.newHashMap();
                ResultSetMetaData metaData = rs.getMetaData();

                String rowkey = rs.getString(1);
                for (int i = 2, ii = metaData.getColumnCount(); i <= ii; ++i) {
                    String key = Rs.lookupColumnName(metaData, i);
                    String value = rs.getString(i);
                    tenantProperties.put(key, value);
                }

                String url = Mtcps.interpret(urlTemplate, tenantProperties);
                tenantProperties.put("url", url);
                tenantPropertiesMap.put(rowkey, tenantProperties);

                return null;
            }
        }).execute("select * from " + tenantPropsTable);
    }
}
