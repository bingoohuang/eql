package org.n3r.eql;

import org.junit.Test;

import java.sql.Timestamp;

public class ForTest {
    @Test
    public void test1() {
        new Eql().id("dropTestTable").execute();
        new Eql().id("createTestTable").params(new Timestamp(1383122146000l)).execute();

        //SimpleTest.Bean bean1 = new Eql().selectFirst("selectIf").params(map).execute();
    }
}
