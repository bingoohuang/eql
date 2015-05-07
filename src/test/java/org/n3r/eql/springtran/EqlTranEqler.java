package org.n3r.eql.springtran;

import org.n3r.eql.eqler.annotations.Eqler;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;

@Eqler
@EqlerConfig("mysql")
public interface EqlTranEqler {
    @Sql({
            "drop table if exists tran",
            "create table tran (a varchar(10) )"
    })
    void prepareData();

    @Sql("insert into tran values(##)")
    int addOneRecord(String value);

    @Sql("select count(1) from tran")
    int queryRecordCounts();
}
