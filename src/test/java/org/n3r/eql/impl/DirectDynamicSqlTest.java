package org.n3r.eql.impl;

import com.google.common.collect.Lists;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.Eql;

import java.util.ArrayList;

public class DirectDynamicSqlTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql("mysql").execute("drop table if exists eql_directdynamic");
        new Eql("mysql").execute("create table eql_directdynamic(id tinyint(1) primary key)");
    }

    @Test
    public void test1() {
        ArrayList<String> ids = Lists.newArrayList("a", "b");
        new Eql("mysql").limit(1)
                .params(ids)
                .execute("select 1 from eql_directdynamic where 'a' in (/* in _1 */) limit 1");
    }
}
