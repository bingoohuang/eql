package org.n3r.eql.eqler;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.Eql;
import org.n3r.eql.EqlTran;
import org.n3r.eql.impl.EqlBatch;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TranBatchEqlerTest {
    static TranBatchEqler eqler;

    @BeforeClass
    public static void beforeClass() {
        eqler = EqlerFactory.getEqler(TranBatchEqler.class);
        eqler.prepareData();
    }

    @Before
    public void before() {
        eqler.cleanCnt();
        assertThat(eqler.queryCnt(), is(0));
    }

    @Test
    public void tranRollback() {
        EqlTran eqlTran = new Eql("mysql").newTran();
        eqlTran.start();
        eqler.incrCnt(eqlTran, 10);
        eqler.incrCnt(eqlTran, 20);
        eqlTran.rollback();

        assertThat(eqler.queryCnt(), is(0));
    }

    @Test
    public void tranCommit() {
        EqlTran eqlTran = new Eql("mysql").newTran();
        eqlTran.start();
        eqler.incrCnt(eqlTran, 11);
        eqler.incrCnt(eqlTran, 22);
        eqlTran.commit();

        assertThat(eqler.queryCnt(), is(33));
    }

    @Test
    public void incrCntBatch() {
        EqlTran eqlTran = new Eql("mysql").newTran();
        eqlTran.start();
        EqlBatch eqlBatch = new EqlBatch(2);

        eqler.incrCntBatch(11, eqlTran, eqlBatch);
        eqler.incrCntBatch(22, eqlTran, eqlBatch);
        eqler.incrCntBatch(33, eqlTran, eqlBatch);
        eqlBatch.executeBatch();

        eqlTran.commit();
        eqlTran.close();

        assertThat(eqler.queryCnt(), is(66));
    }

    @Test
    public void tranDecrCommit() {
        EqlTran eqlTran = new Eql("mysql").newTran();
        eqlTran.start();
        eqler.decrCnt(11, eqlTran);
        eqler.decrCnt(22, eqlTran);
        eqlTran.commit();

        assertThat(eqler.queryCnt(), is(-33));
    }
}
