package org.n3r.eql.eqler;

import org.n3r.eql.EqlTranable;
import org.n3r.eql.eqler.annotations.EqlConfig;
import org.n3r.eql.eqler.annotations.Sql;

@EqlConfig("mysql")
public interface EqlTranableEqler extends EqlTranable {
    @Sql({
            "drop table if exists tran_batch_eqler",
            "create table tran_batch_eqler(cnt int)",
            "insert into tran_batch_eqler values(0)"
    })
    void prepareData();

    @Sql("update tran_batch_eqler set cnt = 0")
    void cleanCnt();

    @Sql("select cnt from tran_batch_eqler")
    int queryCnt();

    @Sql("update tran_batch_eqler set cnt = cnt + ##")
    int incrCnt(int incr);

    @Sql("update tran_batch_eqler set cnt = cnt - ##")
    int decrCnt(int incr);
}
