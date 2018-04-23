package org.n3r.eql.impl;

import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.Eql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class NumberStringAutoNullTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql("mysql").options("NoWhere").execute("DROP TABLE IF EXISTS EQL_NUMBER", "CREATE TABLE EQL_NUMBER(ID INT)");
    }

    @Test
    public void test1() {
        new Eql("mysql").options("NoWhere").params("").execute("DELETE FROM EQL_NUMBER", "INSERT INTO EQL_NUMBER VALUES(#:Number#)");
        Object id = new Eql("mysql").limit(1).execute("SELECT ID FROM EQL_NUMBER");
        assertNull(id);
    }

    @Test
    public void test2() {
        new Eql("mysql").options("NoWhere").params("  ").execute("DELETE FROM EQL_NUMBER", "INSERT INTO EQL_NUMBER VALUES(#:Number#)");
        Object id = new Eql("mysql").limit(1).execute("SELECT ID FROM EQL_NUMBER");
        assertNull(id);
    }

    @Test
    public void test3() {
        new Eql("mysql").options("NoWhere").params(" 123 ").execute("DELETE FROM EQL_NUMBER", "INSERT INTO EQL_NUMBER VALUES(#:Number#)");
        int id = new Eql("mysql").limit(1).returnType("int").execute("SELECT ID FROM EQL_NUMBER");
        assertThat(id, is(equalTo(123)));
    }
}
