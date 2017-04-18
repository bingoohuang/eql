package org.n3r.eql.impl;

import org.junit.Test;
import org.n3r.eql.Eql;

import static com.google.common.truth.Truth.assertThat;

/**
 * Created by bingoohuang on 2017/3/29.
 */
public class DoubleReturnTest {
    @Test
    public void test() {
        double d = new Eql("h2").limit(1).returnType(double.class).execute("select 1.");
        assertThat(d).isWithin(0.1).of(1.);
    }

    @Test
    public void testBig() {
        Double d = new Eql("h2").limit(1).returnType(Double.class).execute("select 1.");
        assertThat(d).isWithin(0.1).of(1.);
    }
}
