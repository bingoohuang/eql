package org.n3r.eql;

import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Timestamp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class InnerVariablsTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql().id("dropTestTable").execute();
        new Eql().id("createTestTable").params(new Timestamp(1383122146000l)).execute();
    }

    @Test
    public void testParams1() {
        Integer result = new Eql().id("testParams1")
                .params(100323, "D", "xxxyyy", new Timestamp(1383122146000l), 132938)
                .execute();
        assertThat(result, is(1));
    }

    @Test
    public void testLastResult() {
        Integer result = new Eql().id("testLastResult")
                .params(new Timestamp(1383122146000l))
                .execute();
    }
}
