package org.n3r.eql.eqler;

import org.n3r.eql.Eql;
import org.n3r.eql.EqlTran;
import org.n3r.eql.impl.EqlBatch;

public class TranBatchEqlerDemo implements TranBatchEqler {
    public void prepareData() {
        new Eql().me().execute("xxx", "yyy");
    }

    @Override
    public void cleanCnt() {

    }

    public int queryCnt() {
        return new Eql().me().execute();
    }

    public int incrCnt(EqlTran eqlTran, int incr) {
        return new Eql().me().useTran(eqlTran).params(incr).execute();
    }

    @Override
    public int incrCntBatch(int incr, EqlTran eqlTran, EqlBatch eqlBatch) {
        return new Eql().me().useTran(eqlTran).useBatch(eqlBatch).params(incr).execute();
    }

    @Override
    public int decrCnt(int incr, EqlTran eqlTran) {
        return 0;
    }

    public static void main(String[] args) {
        new TranBatchEqlerDemo();
    }
}
