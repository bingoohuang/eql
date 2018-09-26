package org.n3r.eql.mtcp;

public interface MtcpEnvironmentAware {
    String getTenantId();

    String getTenantDatabase();
}
