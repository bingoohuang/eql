package org.n3r.eql.impl;

import lombok.val;
import org.junit.Test;
import org.n3r.eql.Eql;
import org.n3r.eql.eqler.EqlerFactory;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;

import static com.google.common.truth.Truth.assertThat;

/**
 * Created by bingoohuang on 2017/3/29.
 */
public class FloatReturnTest {
    @EqlerConfig("h2")
    public interface FloatDao {
        @Sql("select 1")
        float sum1();
        @Sql("select 1")
        Float sum2();
    }

    @Test
    public void test() {
        float d = sum1();
        assertThat(d).isWithin(0.1f).of(1.0f);
    }

    private float sum1() {
        return new Eql("h2").limit(1).returnType(float.class).execute("select 1.");
    }

    @Test
    public void testBig() {
        Float d = sum2();
        assertThat(d).isWithin(0.1f).of(1.0f);
    }

    private Float sum2() {
        return new Eql("h2").limit(1).returnType(Float.class).execute("select 1.");
    }

    @Test
    public void testEqler() {
        val floatDao = EqlerFactory.getEqler(FloatDao.class);
        float sum1 = floatDao.sum1();
        assertThat(sum1).isWithin(0.1f).of(1.0f);

        Float sum2 = floatDao.sum2();
        assertThat(sum2).isWithin(0.1f).of(1.0f);
    }
}
