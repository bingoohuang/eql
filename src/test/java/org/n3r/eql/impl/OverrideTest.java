package org.n3r.eql.impl;

import org.junit.Test;
import org.n3r.eql.Eql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OverrideTest {
    @Test
    public void test1() {
        String x = new Eql("mysql").selectFirst("selectX").execute();
        assertThat(x, is(equalTo("XX")));
    }
}
