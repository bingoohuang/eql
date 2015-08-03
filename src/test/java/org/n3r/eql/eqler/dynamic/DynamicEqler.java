package org.n3r.eql.eqler.dynamic;

import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.eqler.annotations.*;

import java.util.Map;

@EqlerConfig("me")
public interface DynamicEqler {
    @Sql("select '$$' as hello, ## as world")
    Map<String, String> echo(@Dynamic String hello, String world);

    @Sql("select '$$' as hello, ## as shared, ## as world")
    Map<String, String> echoShared(@Dynamic(sole = false) String hello, String world);

    @Sql("select '$hello$' as hello, #world# as world")
    Map<String, String> echoNamed(@Dynamic(name = "hello") String hello, @Param("world") String world, @SqlId String id);

    Map<String, String> echoNamedWithSqlId(@Dynamic(name = "hello") String hello, @Param("world") String world, @SqlId String id);

    @Sql("select '$hello$' as hello, #hello# as shared, #world# as world")
    Map<String, String> echoShareNamed(@Param("hello")
                                       @Dynamic(sole = false, name = "hello")
                                       String hello,
                                       @Param("world") String world);

    @Sql("select 'abc'")
    String eqlConfig(EqlConfig eqlConfig);
}
