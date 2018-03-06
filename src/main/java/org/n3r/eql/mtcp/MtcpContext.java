package org.n3r.eql.mtcp;

import org.slf4j.MDC;

public class MtcpContext {
    public static final String GROUP_TENANT_ID = "groupTenantId";
    public static final String GROUP_TENANT_CODE = "groupTenantCode";
    public static final String TENANT_ID = "tenantId";
    public static final String TENANT_CODE = "tenantCode";

    private static ThreadLocal<String> groupTenantIdLocal = new InheritableThreadLocal<String>();
    private static ThreadLocal<String> groupTenantCodeLocal = new InheritableThreadLocal<String>();
    private static ThreadLocal<String> tenantIdLocal = new InheritableThreadLocal<String>();
    private static ThreadLocal<String> tenantCodeLocal = new InheritableThreadLocal<String>();

    public static void setGroupTenantId(String groupTenantId) {
        groupTenantIdLocal.set(groupTenantId);
        MDC.put(GROUP_TENANT_ID, groupTenantId);
    }

    public static void setGroupTenantCode(String groupTenantCode) {
        groupTenantIdLocal.set(groupTenantCode);
        MDC.put(GROUP_TENANT_CODE, groupTenantCode);
    }

    public static void setTenantId(String tenantId) {
        tenantIdLocal.set(tenantId);
        MDC.put(TENANT_ID, tenantId);
    }

    public static void setTenantCode(String tenantCode) {
        tenantCodeLocal.set(tenantCode);
        MDC.put(TENANT_CODE, tenantCode);
    }

    public static String getGroupTenantId() {
        return groupTenantIdLocal.get();
    }

    public static String getGroupTenantCode() {
        return groupTenantCodeLocal.get();
    }

    public static String getTenantId() {
        return tenantIdLocal.get();
    }

    public static String getTenantCode() {
        return tenantCodeLocal.get();
    }

    public static void clearGroupTenant() {
        clearGroupTenantId();
        clearGroupTenantCode();
    }

    private static void clearGroupTenantId() {
        String groupTenantId = getGroupTenantId();
        if (groupTenantId != null) MDC.remove(GROUP_TENANT_ID);
        groupTenantIdLocal.remove();
    }

    private static void clearGroupTenantCode() {
        String groupTenantCode = getGroupTenantCode();
        if (groupTenantCode != null) MDC.remove(GROUP_TENANT_CODE);
        groupTenantCodeLocal.remove();
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
        clearGroupTenant();
        clearTenant();
    }
}
