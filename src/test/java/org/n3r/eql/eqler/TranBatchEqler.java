package org.n3r.eql.eqler;

import org.n3r.eql.EqlTran;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;
import org.n3r.eql.eqler.annotations.SqlOptions;
import org.n3r.eql.impl.EqlBatch;

@EqlerConfig("mysql")
public interface TranBatchEqler {
    @Sql({
            "drop table if exists tran_batch_eqler",
            "create table tran_batch_eqler(cnt int)",
            "insert into tran_batch_eqler values(0)"
    })
    void prepareData();

    @Sql("update tran_batch_eqler set cnt = 0")
    @SqlOptions("NoWhere")
    void cleanCnt();

    @Sql("select cnt from tran_batch_eqler")
    int queryCnt();

    @Sql("update tran_batch_eqler set cnt = cnt + ##")
    @SqlOptions("NoWhere")
    int incrCnt(EqlTran eqlTran, int incr);

    @Sql("update tran_batch_eqler set cnt = cnt + ##")
    @SqlOptions("NoWhere")
    int incrCntBatch(int incr, EqlTran eqlTran, EqlBatch eqlBatch);

    @Sql("update tran_batch_eqler set cnt = cnt - ##")
    @SqlOptions("NoWhere")
    int decrCnt(int incr, EqlTran eqlTran);
}
