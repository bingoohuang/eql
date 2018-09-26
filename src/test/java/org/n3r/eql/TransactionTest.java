package org.n3r.eql;

import lombok.SneakyThrows;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.util.Closes;

import java.sql.Timestamp;

import static com.google.common.base.Throwables.throwIfUnchecked;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TransactionTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql().id("dropTestTable").execute();
        new Eql().id("createTestTable").params(new Timestamp(1383122146000l)).execute();

    }

    @Test
    public void testRollback() {
        new Eql().id("addBean").params(11000, "bingoo", new Timestamp(1383122146000l)).execute();

        String str = new Eql().selectFirst("getBean").params(11000).execute();
        assertThat(str, is("bingoo"));

        rollback(11000, "dingoo");

        str = new Eql().selectFirst("getBean").params(11000).execute();
        assertThat(str, is("bingoo"));
    }

    @Test
    public void testCommit() {
        new Eql().id("addBean").params(21000, "bingoo", new Timestamp(1383122146000l)).execute();
        String str = new Eql().selectFirst("getBean").params(21000).execute();
        assertThat(str, is("bingoo"));

        commit(21000, "dingoo");

        str = new Eql().selectFirst("getBean").params(21000).execute();
        assertThat(str, is("dingoo"));
    }

    private void rollback(int a, String b) {
        EqlTran tran = new Eql().newTran();
        try {
            tran.start();
            new Eql().useTran(tran)
                    .update("updateBean")
                    .params(a, b)
                    .execute();

            tran.rollback();
        } catch (Exception ex) {
            tran.rollback();
            throwIfUnchecked(ex);
            throw new RuntimeException(ex);
        } finally {
            Closes.closeQuietly(tran);
        }
    }

    @SneakyThrows
    private void commit(int a, String b) {
        EqlTran tran = new Eql().newTran();
        try {
            tran.start();
            new Eql().useTran(tran)
                    .update("updateBean")
                    .params(a, b)
                    .execute();

            tran.commit();
        } catch (Exception ex) {
            tran.rollback();
            throw ex;
        } finally {
            Closes.closeQuietly(tran);
        }
    }
}
