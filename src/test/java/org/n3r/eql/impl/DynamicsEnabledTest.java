package org.n3r.eql.impl;

import org.junit.Test;
import org.n3r.eql.Eql;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DynamicsEnabledTest {
    @Test
    public void test1() {
        try {
            new Eql("dynamicsEnabled").dynamics("123").execute("select $$");
            fail("should throw exception");
        } catch (Exception ex) {
            String message = ex.getMessage();
            assertTrue(message.contains("Unknown column '$$' in 'field list'"));
        }
    }

    @Test
    public void test2() {
        new Eql("mysql").dynamics("123").execute("select $$");
    }
}
