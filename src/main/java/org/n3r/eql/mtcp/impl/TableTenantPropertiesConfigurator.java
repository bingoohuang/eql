package org.n3r.eql.mtcp.impl;

import com.google.common.collect.Maps;
import lombok.val;
import org.n3r.eql.Eql;
import org.n3r.eql.diamond.Dql;
import org.n3r.eql.map.EqlRowMapper;
import org.n3r.eql.mtcp.TenantPropertiesConfigurator;
import org.n3r.eql.mtcp.utils.Mtcps;
import org.n3r.eql.spec.ParamsAppliable;
import org.n3r.eql.util.Rs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class TableTenantPropertiesConfigurator implements TenantPropertiesConfigurator, ParamsAppliable {
    static String urlTemplate
            = "jdbc:mysql://{host}:{port}/{dbname}?[useUnicode,characterEncoding,connectTimeout,autoReconnect]";
    Map<String, Map<String, String>> tenantPropertiesMap = Maps.newHashMap();

    @Override
    public Map<String, String> getTenantProperties(String tenantId) {
        val map = tenantPropertiesMap.get(tenantId);
        if (map == null) throw new RuntimeException(tenantId + "'s config is not found");

        return map;
    }

    @Override
    public void applyParams(String[] params) {
        val eqlConfigName = params[0];
        val tenantPropsTable = params[1];
        val dql = params.length >= 3 && "Dql".equalsIgnoreCase(params[2]);

        Eql eql = dql ? new Dql(eqlConfigName) : new Eql(eqlConfigName);
        eql.returnType(new EqlRowMapper() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum, boolean isSingleColumn) throws SQLException {
                Map<String, String> tenantProperties = Maps.newHashMap();
                val metaData = rs.getMetaData();

                String rowkey = rs.getString(1);
                for (int i = 2, ii = metaData.getColumnCount(); i <= ii; ++i) {
                    val key = Rs.lookupColumnName(metaData, i);
                    val value = rs.getString(i);
                    tenantProperties.put(key, value);
                }

                val url = Mtcps.interpret(urlTemplate, tenantProperties);
                tenantProperties.put("url", url);
                tenantPropertiesMap.put(rowkey, tenantProperties);

                return null;
            }
        }).execute("select * from " + tenantPropsTable);
    }
}
