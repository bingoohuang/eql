package org.n3r.eql.eqler.dynamic;

import org.n3r.eql.Eql;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.eqler.annotations.Dynamic;
import org.n3r.eql.eqler.annotations.Param;

import java.util.Map;

public class DynamicEqlerDemo implements DynamicEqler {
    @Override
    public Map<String, String> echo(@Dynamic String hello, String world) {
        return null;
    }

    @Override
    public Map<String, String> echoShared(@Dynamic(sole = false) String hello, String world) {
        return null;
    }

    @Override
    public Map<String, String> echoNamed(@Dynamic(name = "hello") String hello, @Param("world") String world) {
        return null;
    }

    @Override
    public Map<String, String> echoShareNamed(@Param("hello") @Dynamic(sole = false, name = "hello") String hello, @Param("world") String world) {
        return null;
    }

    public String eqlConfig(EqlConfig eqlConfig) {
        return new Eql(eqlConfig).execute("xxx");
    }
}
