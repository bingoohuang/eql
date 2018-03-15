package org.n3r.eql.mtcp;

import org.slf4j.MDC;

public class MtcpContext {
    public static final String TENANT_ID = "tenantId";
    public static final String TENANT_CODE = "tenantCode";

    private static ThreadLocal<String> tenantIdLocal = new InheritableThreadLocal<String>();
    private static ThreadLocal<String> tenantCodeLocal = new InheritableThreadLocal<String>();


    public static void setTenantId(String tenantId) {
        tenantIdLocal.set(tenantId);
        MDC.put(TENANT_ID, tenantId);
    }

    public static void setTenantCode(String tenantCode) {
        tenantCodeLocal.set(tenantCode);
        MDC.put(TENANT_CODE, tenantCode);
    }

    public static String getTenantId() {
        return tenantIdLocal.get();
    }

    public static String getTenantCode() {
        return tenantCodeLocal.get();
    }

    public static void clearTenant() {
        clearTenantId();
        clearTenantCode();
    }

    private static void clearTenantCode() {
        String tenantCode = getTenantCode();
        if (tenantCode != null) MDC.remove(TENANT_CODE);
        tenantCodeLocal.remove();
    }

    private static void clearTenantId() {
        String tenantId = getTenantId();
        if (tenantId != null) MDC.remove(TENANT_ID);
        tenantIdLocal.remove();
    }

    public static void clear() {
        clearTenant();
    }
}
