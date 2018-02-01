package org.n3r.eql.eqler.spring;

import org.n3r.eql.EqlPage;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.ProfiledSql;
import org.n3r.eql.eqler.annotations.ProfiledSqls;
import org.n3r.eql.eqler.annotations.Sql;

import java.util.List;

@EqlerConfig(value = "h2", createClassFileForDiagnose = true)
public interface SpEqler {
    int queryOne();

    @Sql("select 'o2m'")
    String queryLower();

    @Sql("select 'a' as a union all select 'b' as a union all select 'c' as a")
    List<ABean> queryLowers(EqlPage eqlPage);

    @ProfiledSqls({
            @ProfiledSql(profile = "dev", sql = "select 'devdev'"),
            @ProfiledSql(profile = "prod", sql = "select 'prodprod'")
    })
    String queryProfileName();
}
