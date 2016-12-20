package org.n3r.eql;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LikeTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql("mysql").id("before").execute();
    }

    @AfterClass
    public static void afterClass() {
        new Eql("mysql").id("after").execute();
    }

    @Test
    public void test() {
        String x = new Eql("mysql").id("likeDemo").params("b").limit(1).execute();
        assertEquals("x", x);

        x = new Eql("mysql").id("likeDemo").params("x").limit(1).execute();
        assertNull(x);

        x = new Eql("mysql").id("leftLikeDemo").params("c").limit(1).execute();
        assertEquals("x", x);

        x = new Eql("mysql").id("leftLikeDemo").params("x").limit(1).execute();
        assertNull(x);

        x = new Eql("mysql").id("rightLikeDemo").params("a").limit(1).execute();
        assertEquals("x", x);

        x = new Eql("mysql").id("rightLikeDemo").params("x").limit(1).execute();
        assertNull(x);
    }

    @Test
    public void testEscape() {
        String x = new Eql("mysql").id("likeWithEscape").params("b").limit(1).execute();
        assertEquals("x", x);

        x = new Eql("mysql").id("likeWithEscape").params("%").limit(1).execute();
        assertNull(x);
    }
}
