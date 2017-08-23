package org.n3r.eql.mtcp.impl;

import org.n3r.eql.mtcp.TenantPropertiesConfigurator;
import org.n3r.eql.mtcp.utils.Mtcps;
import org.n3r.eql.mtcp.utils.StringTable;
import org.n3r.eql.spec.ParamsAppliable;

import java.util.Map;

public class ConfigedTenantPropertiesConfigurator
        implements TenantPropertiesConfigurator, ParamsAppliable {
    private StringTable stringTable;

    @Override
    public Map<String, String> getTenantProperties(String tenantId) {
        Map<String, String> tenantProperties = stringTable.findRow(tenantId);

        String url = Mtcps.interpret(urlTemplate, tenantProperties);
        tenantProperties.put("url", url);

        return tenantProperties;
    }

    private static String urlTemplate = "jdbc:mysql://{host}:{port}/{dbname}?[useUnicode,characterEncoding,connectTimeout,autoReconnect]";


    @Override
    public void applyParams(String[] params) {
        this.stringTable = new StringTable(params);
    }
}
