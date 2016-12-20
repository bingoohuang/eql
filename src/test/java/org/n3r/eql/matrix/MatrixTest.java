package org.n3r.eql.matrix;

import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.Eql;
import org.n3r.eql.EqlTran;
import org.n3r.eql.Eqll;
import org.n3r.eql.util.Closes;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MatrixTest {
//    @Rule
//    public TestRule globalTimeout = new Timeout(10000);


    @BeforeClass
    public static void setup() {
        Eqll.choose("matrix");

        EqlMatrixConnection.chooseDbName("dba");
        new Eqll().id("setup").execute();

        EqlMatrixConnection.chooseDbName("dbb");
        new Eqll().id("setup").execute();

        EqlMatrixConnection.chooseDbName("dbc");
        new Eqll().id("setup").execute();
    }

    @Test
    public void test1() {
        new Eql("matrix").id("addPerson").params("a001", "0", "order").execute();
        new Eql("matrix").id("addPerson").params("b001", "1", "bingoo").execute();
        new Eql("matrix").id("addPerson").params("c001", "0", "huang").execute();

        String name = new Eql("matrix").id("getPerson").params("a001").limit(1).execute();
        assertThat(name, is("order"));
        name = new Eql("matrix").id("getPerson").params("b001").limit(1).execute();
        assertThat(name, is("bingoo"));
        name = new Eql("matrix").id("getPerson").params("c001").limit(1).execute();
        assertThat(name, is("huang"));


        new Eql("matrix").id("updatePerson").params("a001", "0", "red").execute();
        new Eql("matrix").id("updatePerson").params("b001", "0", "blue").execute();
        new Eql("matrix").id("updatePerson").params("c001", "1", "black").execute();
    }

    @Test
    public void test2() throws Exception {
        Eql mql = new Eql("matrix");
        EqlTran eqlTran = mql.newTran();

        try {
            eqlTran.start();
            mql.id("addPerson").params("a002", "0", "order123").execute();
            eqlTran.commit();

            String name = new Eql("matrix").id("getPerson").params("a002").limit(1).execute();
            assertThat(name, is("order123"));

        } catch (Exception e) {
            eqlTran.rollback();
            throw e;
        } finally {
            Closes.closeQuietly(eqlTran);
        }
    }
}
