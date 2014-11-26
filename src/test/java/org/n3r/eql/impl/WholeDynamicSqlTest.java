package org.n3r.eql.impl;

import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.Eql;

public class WholeDynamicSqlTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql("mysql").execute();
    }

    @Test
    public void test1() {
        new Eql("mysql").id("test").params(2, "FFKK").execute();
    }

    @Test
    public void test2() {
        new Eql("mysql").id("test").params(1, "FFKK").execute();
    }
}
