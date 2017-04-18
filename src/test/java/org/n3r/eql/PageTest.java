package org.n3r.eql;

import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PageTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql("druid").id("dropTestTable").execute();
        new Eql("druid").id("createTestTable").params(new Timestamp(1383122146000l)).execute();
    }

    @Test
    public void test() {
        EqlPage page = new EqlPage(3, 2);
        List<SimpleTest.Bean> beans = new Eql("druid").id("testPage")
                .returnType(SimpleTest.Bean.class)
                .limit(page)
                .params("DC")
                .execute();
        assertThat(page.getTotalRows(), is(7));
        assertThat(beans.size(), is(2));
        System.out.println(page);
        System.out.println(beans);
    }

    // 在分页SQL在应用启动后，是第一次执行时，由于druid连接池在没有配置drivername时，第一次获取driverName是空，
    // 导致无法识别数据库类型（比如mysql）,无法使用物理分页sql（比如limit 1,3），导致数据全部出来的问题
    @Test
    public void testZhuxiaobo() {
        EqlPage page = new EqlPage(0, 3);
        List<HashMap> beans = new Eql("druid").id("testPage")
                .returnType(HashMap.class)
                .limit(page)
                .params("DC")
                .execute();
        assertThat(page.getTotalRows(), is(7));
        assertThat(beans.size(), is(3));
        System.out.println(page);
        System.out.println(beans);
    }


    @Test
    public void testCountWhenGroupby() {
        EqlPage page = new EqlPage(3, 2);
        List<SimpleTest.Bean> beans = new Eql("druid").id("withGroupby")
                .returnType(SimpleTest.Bean.class)
                .limit(page)
                .execute();
        System.out.println(page);
        System.out.println(beans);
    }
}
