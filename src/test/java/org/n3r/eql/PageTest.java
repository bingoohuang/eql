package org.n3r.eql;

import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.List;

public class PageTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql().id("dropTestTable").execute();
        new Eql().id("createTestTable").params(new Timestamp(1383122146000l)).execute();
    }

    @Test
    public void test() {
        EqlPage page = new EqlPage(3, 2);
        List<SimpleTest.Bean> beans = new Eql().id("testPage")
                .returnType(SimpleTest.Bean.class)
                .limit(page)
                .params("DC")
                .execute();
        System.out.println(page);
        System.out.println(beans);
    }

    @Test
    public void testCountWhenGroupby() {
        EqlPage page = new EqlPage(3, 2);
        List<SimpleTest.Bean> beans = new Eql().id("withGroupby")
                .returnType(SimpleTest.Bean.class)
                .limit(page)
                .execute();
        System.out.println(page);
        System.out.println(beans);
    }
}
