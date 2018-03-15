package org.n3r.eql.mtcp;

public class MyMtcpEnvironment implements MtcpEnvironmentAware {
    static ThreadLocal<String> threadLocal = new ThreadLocal<String>();

    @Override public String getTenantId() {
        return MtcpContext.getTenantId();
    }

    @Override public String getTenantDatabase() {
        return threadLocal.get();
    }
}
