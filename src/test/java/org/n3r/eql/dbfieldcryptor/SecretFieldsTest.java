package org.n3r.eql.dbfieldcryptor;

import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.Eql;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SecretFieldsTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql("secret").id("createTestTable").execute();
    }

    @Test
    public void test1() {
        new Eql("secret").insert("insertSecret")
                .params(1, "bb", "cc")
                .execute();

        Map<String, String> map = new Eql("secret-no").select("selectSecret")
                .limit(1).params(1)
                .execute();

        assertEquals("IPjmXtLPr4mvJWDxKwtr5Q==", map.get("B"));
        assertEquals("A6WN3pHLB+KJdr6Flbt3Lw==", map.get("C"));

        map = new Eql("secret").select("selectSecret")
                .limit(1).params(1)
                .execute();
        assertEquals("bb", map.get("B"));
        assertEquals("cc", map.get("C"));
    }

    @Test
    public void test2() {
        new Eql("secret").insert("insertSecret")
                .params(2, "bb", "cc")
                .execute();

        int rows = new Eql("secret").update("updateSecret")
                .params("cc", "BB")
                .execute();
        assertTrue(rows >= 1);


        Map<String, String> map = new Eql("secret-no").select("selectSecret")
                .limit(1).params(2)
                .execute();

        assertEquals("5qmBcbNm3x/068gmtb2mkw==", map.get("B"));
        assertEquals("A6WN3pHLB+KJdr6Flbt3Lw==", map.get("C"));

        map = new Eql("secret").select("selectSecret")
                .limit(1).params(2)
                .execute();
        assertEquals("BB", map.get("B"));
        assertEquals("cc", map.get("C"));
    }
}
