package org.n3r.eql;

import org.junit.BeforeClass;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class SelectFirstNullTest {
    @BeforeClass
    public static void setup() {
        new Eql("h2").id("setup").execute();
    }

    @Test
    public void test() {
        String xyz = new Eql("h2").selectFirst("xxx").params(123).returnType(String.class).execute();
        assertThat(xyz).isNull();
    }
}
