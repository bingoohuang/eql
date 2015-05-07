package org.n3r.eql.springtran.dao;

import org.n3r.eql.eqler.annotations.Eqler;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Param;
import org.n3r.eql.eqler.annotations.Sql;

/**
 * Created by liolay on 15-5-5.
 */
@EqlerConfig("mysql")
@Eqler
public interface TestDao {
    @Sql({
            "DROP TABLE IF EXISTS TRAN",
            "CREATE TABLE TRAN (  A VARCHAR(10) )"
    })
    void prepareData();

    @Sql("insert into tran values(#a#)")
    int addOneRecord(@Param("a") String value);

    @Sql("select count(1) from tran")
    int queryRecordCounts();
}
