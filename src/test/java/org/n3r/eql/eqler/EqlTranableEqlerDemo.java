package org.n3r.eql.eqler;

import org.n3r.eql.Eql;
import org.n3r.eql.EqlTran;
import org.n3r.eql.trans.EqlTranThreadLocal;

public class EqlTranableEqlerDemo implements EqlTranableEqler {
    public static void main(String[] args) {
        new EqlTranableEqlerDemo();
    }

    @Override
    public void prepareData() {

    }

    @Override
    public void cleanCnt() {

    }

    @Override
    public int queryCnt() {
        return 0;
    }

    @Override
    public int incrCnt(int incr) {
        return new Eql().me().useTran(EqlTranThreadLocal.get()).params(incr).execute("xxx");
    }

    @Override
    public int decrCnt(int incr) {
        return new Eql().me().params(incr).execute("yyyy");
    }

    @Override
    public void start() {
        EqlTran eqlTran = EqlTranThreadLocal.get();
        if (eqlTran != null) return;

        eqlTran = new Eql("mysql").me().newTran();
        EqlTranThreadLocal.set(eqlTran);
    }

    @Override
    public void commit() {
        EqlTran eqlTran = EqlTranThreadLocal.get();
        if (eqlTran != null) eqlTran.commit();
    }

    @Override
    public void rollback() {
        EqlTran eqlTran = EqlTranThreadLocal.get();
        if (eqlTran != null) eqlTran.rollback();
    }

    @Override
    public void close() {
        EqlTran eqlTran = EqlTranThreadLocal.get();
        if (eqlTran != null) {
            EqlTranThreadLocal.clear();
            eqlTran.close();
        }
    }
}
