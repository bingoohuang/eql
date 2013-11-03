package org.n3r.eql;

import org.junit.Test;

public class JndiTest {
    @Test
    public void test() {
        new Eql("jndi").id("createTable").execute();
        new Eql("jndi").id("addData").params("bingoo").execute();
        new Eql("jndi").id("addData").params("huang").execute();
    }
}
