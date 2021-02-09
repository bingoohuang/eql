package org.n3r.eql.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EqlUtilsTest {

    @Test
    public void testTrimLastUnusedPart() {
        String originSql = "select * from some_where ";
        String actualSql = "select * from some_where";
        assertEquals(actualSql, EqlUtils.trimLastUnusedPart(originSql));

        originSql = "select * from some where ";
        actualSql = "select * from some";
        assertEquals(actualSql, EqlUtils.trimLastUnusedPart(originSql));

        originSql = "select * from lalaland\n";
        actualSql = "select * from lalaland";
        assertEquals(actualSql, EqlUtils.trimLastUnusedPart(originSql));

        originSql = "select * from lalal\nand\n";
        actualSql = "select * from lalal";
        assertEquals(actualSql, EqlUtils.trimLastUnusedPart(originSql));

        originSql = "select * from major\r";
        actualSql = "select * from major";
        assertEquals(actualSql, EqlUtils.trimLastUnusedPart(originSql));

        originSql = "select * from maj\ror\r";
        actualSql = "select * from maj";
        assertEquals(actualSql, EqlUtils.trimLastUnusedPart(originSql));
    }
}
