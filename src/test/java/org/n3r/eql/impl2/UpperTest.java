package org.n3r.eql.impl2;

import org.junit.Test;
import org.n3r.eql.Eql;

import static org.junit.Assert.assertEquals;

public class UpperTest {
    @Test
    public void test1() {
        UpperBean bean = new Eql("mysql")
                .returnType(UpperBean.class)
                .limit(1)
                .execute("select 'a' as name");


        assertEquals(bean.getName(), "A");
    }

    @Test
    public void test2() {
        UpperBean bean = new Eql("mysql")
                .returnType(UpperBean.class)
                .limit(1)
                .execute("select 'a' as name, 'BB' as addr");


        assertEquals("A", bean.getName());
        assertEquals("bb", bean.getAddr());
    }

    @Test
    public void test3() {
        UpperBean bean = new Eql("mysql")
                .returnType(UpperBean.class)
                .limit(1)
                .execute("select 'a' as name, 'BB' as addr, " +
                        "STR_TO_DATE('2015-10-09 15:28:49', '%Y-%m-%d %T') as day");


        assertEquals("A", bean.getName());
        assertEquals("bb", bean.getAddr());
        assertEquals("2015-10-09", bean.getDay());
    }
}
