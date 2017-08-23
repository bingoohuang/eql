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
public class DoubleReturnTest {
    @EqlerConfig("h2")
    public interface DoubleDao {
        @Sql("select 1")
        double sum1();

        @Sql("select 1")
        Double sum2();
    }

    @Test
    public void test() {
        double d = sum1();
        assertThat(d).isWithin(0.1).of(1.);
    }

    private double sum1() {
        return new Eql("h2").limit(1).returnType(double.class).execute("select 1.");
    }

    @Test
    public void testBig() {
        Double d = sum2();
        assertThat(d).isWithin(0.1).of(1.);
    }

    private Double sum2() {
        return new Eql("h2").limit(1).returnType(Double.class).execute("select 1.");
    }

    @Test
    public void testEqler() {
        val floatDao = EqlerFactory.getEqler(DoubleDao.class);
        double sum1 = floatDao.sum1();
        assertThat(sum1).isWithin(0.1).of(1.);

        Double sum2 = floatDao.sum2();
        assertThat(sum2).isWithin(0.1).of(1.);
    }
}
