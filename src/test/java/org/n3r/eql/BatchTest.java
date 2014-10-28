package org.n3r.eql;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.impl.EqlBatch;
import org.n3r.eql.util.Closes;

import java.security.SecureRandom;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class BatchTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql().id("beforeClass").execute();
    }

    @Test
    public void test1() {
        EqlTran eqlTran = new Eql().newTran();
        eqlTran.start();
        EqlBatch eqlBatch = new EqlBatch();
        for (int i = 0; i < 10; ++i) {
            String orderNo = randLetters(10);
            String userId = randLetters(10);
            int prizeItem = randInt(10);
            int ret = new Eql().useBatch(eqlBatch).useTran(eqlTran).insert("insertPrizeBingoo")
                    .params(orderNo, "Olympic", "" + prizeItem, userId)
                    .execute();

            assertThat(ret, is(0));
        }

        eqlBatch.executeBatch();
        eqlTran.commit();
        Closes.closeQuietly(eqlTran);
    }


    @Test
    public void test2() {
        EqlTran eqlTran = new Eql().newTran();
        eqlTran.start();

        EqlBatch eqlBatch = new EqlBatch(5);

        for (int i = 0; i < 10; ++i) {
            String orderNo = randLetters(10);
            String userId = randLetters(10);
            int prizeItem = randInt(10);
            int ret = new Eql().useBatch(eqlBatch).useTran(eqlTran).insert("insertPrizeBingoo")
                    .params(orderNo, "Olympic", "" + prizeItem, userId)
                    .execute();

            assertThat(ret, is(0));
        }

        eqlBatch.executeBatch();
        eqlTran.commit();
        Closes.closeQuietly(eqlTran);

        String str = new Eql().id("test").limit(1).execute();
        assertThat(str, is("x"));
    }

    /**
     * only update/insert/delete sql can be wrapped in an execute batch.
     */
    @Test(expected = EqlExecuteException.class)
    public void test3() {
        EqlTran eqlTran = new Eql().newTran();
        eqlTran.start();
        EqlBatch eqlBatch = new EqlBatch(5);

        for (int i = 0; i < 10; ++i) {
            new Eql().useBatch(eqlBatch).useTran(eqlTran).id("test").limit(1).execute();
            String orderNo = randLetters(10);
            String userId = randLetters(10);
            int prizeItem = randInt(10);
            int ret = new Eql().useBatch(eqlBatch).useTran(eqlTran).insert("insertPrizeBingoo")
                    .params(orderNo, "Olympic", "" + prizeItem, userId)
                    .execute();

            assertThat(ret, is(0));
        }

        eqlBatch.executeBatch();
        Closes.closeQuietly(eqlTran);
    }


    private int randInt(int i) {
        return random.nextInt(i);
    }

    static SecureRandom random = new SecureRandom();

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
