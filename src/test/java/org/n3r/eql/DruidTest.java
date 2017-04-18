package org.n3r.eql;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DruidTest {
    @Test
    public void test1() {
        String x = new Eql("druid").limit(1).execute("select 'X'");
        assertEquals("X", x);
    }
}
