package org.n3r.eql.mtcp;

import org.junit.Test;
import org.n3r.eql.Eql;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class MtcpContextTest {
    @Test
    public void test() {
        MtcpContext.setTenantId("T001");

        assertThat(MtcpContext.getTenantId(), is(equalTo("T001")));

        MtcpContext.clear();
        assertThat(MtcpContext.getTenantId(), is(nullValue()));
    }

    @Test
    public void testDruidMtcp() {
        MtcpContext.setTenantId("test-group-dep1");
        MyMtcpEnvironment.threadLocal.set("test-group-dep1");

        String name = new Eql("druid-mtcp").limit(1).execute("select name from bingoo_transaction_1");
        assertThat(name, is(equalTo("111")));

        name = new Eql("druid-mtcp").limit(1).execute("select name from bingoo_transaction_1");
        assertThat(name, is(equalTo("111")));

        MtcpContext.setTenantId("test-group-dep2");
        MyMtcpEnvironment.threadLocal.set("test-group-dep2");

        name = new Eql("druid-mtcp").limit(1).execute("select name from bingoo_transaction_2");
        assertThat(name, is(equalTo("222")));
    }
}
