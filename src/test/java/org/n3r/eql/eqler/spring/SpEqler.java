package org.n3r.eql.eqler.spring;

import org.n3r.eql.EqlPage;
import org.n3r.eql.diamond.Dql;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;

import java.util.List;

@EqlerConfig(value = "mysql", eql = Dql.class)
public interface SpEqler {
    int queryOne();

    @Sql("select 'o2m'")
    String queryLower();

    @Sql("select 'a' as a union all select 'b' as a union all select 'c' as a")
    List<String> queryLowers(EqlPage eqlPage);
}
