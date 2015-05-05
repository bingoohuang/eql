package org.n3r.eql.mtcp;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class MtcpContextTest {
    @Test
    public void test() {
        MtcpContext.setTenantId("T001");

        assertThat(MtcpContext.getTenantId(), is(equalTo("T001")));

        MtcpContext.clearTenantId();
        assertThat(MtcpContext.getTenantId(), is(nullValue()));
    }
}
