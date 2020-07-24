package org.n3r.eql;

import lombok.Getter;
import lombok.Setter;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;

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
        assertThat(result, is(1));

        EqlTest result2 = new Eql().id("testLastResult2")
                .params(new Timestamp(1383122146000l))
                .returnType(EqlTest.class).limit(1)
                .execute();
        assertThat(result2.getA(), is(4123));
        assertThat(result2.getB(), is("D"));
        assertThat(result2.getC(), is("ABC"));
        assertThat(result2.getD(), is(new Date(1383122146000l)));
        assertThat(result2.getE(), is(104));
    }

    @Getter
    @Setter
    static class EqlTest {

        private int a;
        private String b;
        private String c;
        private Date d;
        private int e;
    }
}
