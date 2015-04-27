package org.n3r.eql;

import org.junit.Assert;
import org.junit.Test;


public class DelayParseTest {
    @Test
    public void test() {
        new Eql().id("test1").limit(1).execute();

        try {
            new Eql().id("badTest2").execute();
            Assert.fail();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test(expected = RuntimeException.class)
    public void testParseImmediately() {
         new Eql("parseImmediately").id("badTest2").execute();
    }
}
