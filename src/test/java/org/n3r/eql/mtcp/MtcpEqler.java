package org.n3r.eql.mtcp;

import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;

@EqlerConfig("mtcp")
public interface MtcpEqler {
    @Sql("select cnt from mtcp")
    int queryCnt();
}
