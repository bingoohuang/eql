package org.n3r.eql.mtcp;

import java.util.Map;

public interface TenantPropertiesConfigurator {
    Map<String, String> getTenantProperties(String tenantId);
}
