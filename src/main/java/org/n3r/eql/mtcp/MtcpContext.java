package org.n3r.eql.mtcp;

import org.slf4j.MDC;

public class MtcpContext {
    private static ThreadLocal<String> tenantIdLocal = new InheritableThreadLocal<String>();
    private static ThreadLocal<String> tenantCodeLocal = new InheritableThreadLocal<String>();

    public static void setTenantId(String tenantId) {
        tenantIdLocal.set(tenantId);
        MDC.put("tenantId", tenantId);
    }

    public static void setTenantCode(String tenantCode) {
        tenantCodeLocal.set(tenantCode);
        MDC.put("tenantCode", tenantCode);
    }

    public static String getTenantId() {
        return tenantIdLocal.get();
    }
    public static String getTenantCode() {
        return tenantCodeLocal.get();
    }

    public static void clear() {
        MDC.remove(tenantIdLocal.get());
        MDC.remove(tenantCodeLocal.get());
        tenantIdLocal.remove();
        tenantCodeLocal.remove();
    }
}
