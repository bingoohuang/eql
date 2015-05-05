package org.n3r.eql.mtcp;

import org.slf4j.MDC;

public class MtcpContext {
    private static ThreadLocal<String> tenantIdLocal = new InheritableThreadLocal<String>();

    public static void setTenantId(String tenantId) {
        tenantIdLocal.set(tenantId);
        MDC.put("tenantId", tenantId);
    }

    public static String getTenantId() {
        return tenantIdLocal.get();
    }

    public static void clearTenantId() {
        MDC.remove(tenantIdLocal.get());
        tenantIdLocal.remove();
    }
}
