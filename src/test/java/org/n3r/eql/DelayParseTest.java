package org.n3r.eql;

import org.junit.Assert;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;


public class DelayParseTest {
    @Test
    public void test() {
        new Eql().id("test1").limit(1).execute();

        try {
            new Eql().id("badTest2").execute();
            Assert.fail();
        } catch (Exception ex) {
            assertThat(ex.getMessage().contains("if is invalid"));
        }
    }

    @Test(expected = RuntimeException.class)
    public void testParseImmediately() {
        new Eql("parseImmediately").id("badTest2").execute();
    }
}
