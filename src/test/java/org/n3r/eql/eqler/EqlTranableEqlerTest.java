package org.n3r.eql.eqler;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EqlTranableEqlerTest {
    static EqlTranableEqler eqler;

    @BeforeClass
    public static void beforeClass() {
        eqler = EqlerFactory.getEqler(EqlTranableEqler.class);
        eqler.prepareData();
    }

    @Before
    public void before() {
        eqler.cleanCnt();
        assertThat(eqler.queryCnt(), is(0));
    }

    @Test
    public void tranRollback() {
        eqler.start();
        eqler.incrCnt(10);
        eqler.incrCnt(20);
        eqler.rollback();
        eqler.close();

        assertThat(eqler.queryCnt(), is(0));
    }

    @Test
    public void tranCommit() {
        eqler.start();
        eqler.incrCnt(11);
        eqler.incrCnt(22);
        eqler.commit();
        eqler.close();

        assertThat(eqler.queryCnt(), is(33));
    }

    @Test
    public void tranDecrCommit() {
        eqler.start();
        eqler.decrCnt(11);
        eqler.decrCnt(22);
        eqler.commit();
        eqler.close();

        assertThat(eqler.queryCnt(), is(-33));
    }
}
