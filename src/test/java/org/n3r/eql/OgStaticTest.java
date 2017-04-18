package org.n3r.eql;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class OgStaticTest {
    public static String STATE = "102";

    @Test
    public void test() {
        String str = new Eql("mysql").id("ognlStatic").limit(1)
                .params(ImmutableMap.of("state", "102", "x", "y"))
                .execute();
        assertThat(str, is(nullValue()));

        str = new Eql("mysql").id("ognlStatic").limit(1)
                .params(ImmutableMap.of("state", "103", "x", "x"))
                .execute();
        assertThat(str, is("X"));
    }
}
