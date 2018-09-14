package org.n3r.eql.impl2;

import com.google.common.collect.Maps;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.Eql;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class DefaultValueTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql("h2").execute(
                "DROP TABLE IF EXISTS T_DefaultValueTest",
                "CREATE TABLE T_DefaultValueTest (ID INT, NAME VARCHAR(10))",
                "INSERT INTO T_DefaultValueTest values(1,'BINGOO')");
    }

    @Test
    public void testSeq() {
        String id = new Eql("h2")
                .returnType(String.class)
                .limit(1)
                .execute("select id from T_DefaultValueTest where NAME= #1:!BINGOO#");


        assertEquals(id, "1");

        String id2 = new Eql("h2")
                .returnType(String.class)
                .params(null)
                .limit(1)
                .execute("select id from T_DefaultValueTest where NAME= #1:!BINGOO#");


        assertEquals(id2, "1");
    }

    @Test
    public void testName() {
        HashMap<Object, Object> map = Maps.newHashMap();
        map.put("name", null);
        String id = new Eql("h2")
                .returnType(String.class)
                .params(map)
                .limit(1)
                .execute("select id from T_DefaultValueTest where NAME= #name:!BINGOO#");


        assertEquals(id, "1");
    }

    @Test
    public void testQuestion() {
        HashMap<Object, Object> map = Maps.newHashMap();
        String id = new Eql("h2")
                .returnType(String.class)
                .params(map)
                .limit(1)
                .execute("select id from T_DefaultValueTest where NAME= #?:!BINGOO#");


        assertEquals(id, "1");
    }
}
