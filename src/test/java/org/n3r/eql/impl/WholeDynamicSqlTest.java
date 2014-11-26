package org.n3r.eql.impl;

import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.Eql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class WholeDynamicSqlTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql("mysql").execute();
    }

    @Test
    public void test1() {
        Eql eql = new Eql("mysql").id("test");
        eql.params(2, "FFKK").execute();
        assertThat(eql.getEqlRun().getEvalSql(), is(equalTo("UPDATE EQL_IN SET NAME = 'FFKK' WHERE ID = 2")));
    }

    @Test
    public void test2() {
        Eql eql = new Eql("mysql").id("test");
        eql.params(1, "FFKK").execute();
        assertThat(eql.getEqlRun().getRunSql(), is(equalTo("INSERT INTO EQL_IN VALUES(100, 'XX')")));
    }

    @Test
    public void test3() {
        Eql eql1 = new Eql("mysql").id("test3");
        eql1.params(1).execute();
        assertThat(eql1.getEqlRun().getRunSql(), is(equalTo("INSERT INTO EQL_IN VALUES(100, 'XX')")));

        Eql eql2 = new Eql("mysql").id("test3");
        eql2.params(2).execute();
        assertThat(eql2.getEqlRun(), is(nullValue()));

    }
}
