package org.n3r.eql;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.SecureRandom;

import static junit.framework.Assert.assertEquals;

public class BatchTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql().id("beforeClass").execute();
    }

    @Test
    public void test1() {
        Eql esql = new Eql();
        esql.startBatch();
        for (int i = 0; i < 10; ++i) {
            String orderNo = randLetters(10);
            String userId = randLetters(10);
            int prizeItem = randInt(10);
            int ret = esql.insert("insertPrizeBingoo")
                    .params(orderNo, "Olympic", "" + prizeItem, userId)
                    .execute();

            assertEquals(0, ret);
        }

        esql.executeBatch();
    }

    @Test
    public void test2() {
        Eql esql = new Eql();

        esql.startBatch(5);
        for (int i = 0; i < 10; ++i) {
            String orderNo = randLetters(10);
            String userId = randLetters(10);
            int prizeItem = randInt(10);
            int ret = esql.insert("insertPrizeBingoo")
                    .params(orderNo, "Olympic", "" + prizeItem, userId)
                    .execute();

            assertEquals(0, ret);
        }

        esql.executeBatch();
    }

    private int randInt(int i) {
        return random.nextInt(i);
    }

    static  SecureRandom random = new SecureRandom();
    private String randLetters(int len) {
        StringBuilder str = new StringBuilder(len);
        for (int i = 0; i < len; ++i) {
            char ch = (char) ('A' + random.nextInt(26));
            str.append(ch);
        }

        return str.toString();
    }


    @AfterClass
    public static void afterClass() {
        new Eql().id("afterClass").execute();
    }
}
