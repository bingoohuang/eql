package org.n3r.eql.eqler;

import org.n3r.eql.EqlPage;
import org.n3r.eql.eqler.annotations.*;

import java.util.List;
import java.util.Map;

@MyEqlerConfig
public interface MyEqler {
    String queryOne();

    @UseSqlFile("org/n3r/eql/eqler/MyEqlerTwo.eql")
    String queryTwo();

    @UseSqlFile(clazz = MyEqler.class)
    int queryThree();

    @Sql("select 4")
    long queryFour();

    @Sql("select 1")
    boolean queryTrue();

    @Sql("select 0")
    boolean queryFalse();

    String queryById(String id);

    String queryByMap(Map<String, String> map);

    @Sql("select #userId# as userId, #merchantId# as merchantId, #id# as id, #name# as name")
    Map queryByMap(@Param("userId") long userId, @Param("merchantId") String merchantId
            , @Param("id") int id, @Param("name") String name);

    MyEqlerBean queryBean(String id);

    @SqlId("queryBean")
    MyEqlerBean queryBeanX(String id);

    List<MyEqlerBean> queryBeans(String id);

    @Sql("select 1")
    String queryDirectSql();

    List<MyEqlerBean> queryMoreBeans(int a, EqlPage eqlPage, int b);
}
