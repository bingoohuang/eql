package org.n3r.eql;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test eql caching based on query result.
 */
public class CacheTest {
    @Test
    public void test1() throws InterruptedException {
        String str1 = new Eql().limit(1).execute();
        TimeUnit.SECONDS.sleep(1);
        String str2 = new Eql().limit(1).execute();
        String str3 = new Eql().cached(false).limit(1).execute();
        TimeUnit.SECONDS.sleep(2);
        String str4 = new Eql().limit(1).execute();

        assertThat(str1, is(str2));
        assertThat(str1, is(not(str3)));
        assertThat(str1, is(not(str4)));
    }

    @Test
    public void test2() throws InterruptedException {
        String str1 = new Eql().limit(1).execute();
        TimeUnit.SECONDS.sleep(1);
        String str2 = new Eql().limit(1).execute();

        assertThat(str1, is(not(str2)));
    }

    @Test
    public void test3() throws InterruptedException {
        EqlPage eqlPage = new EqlPage(0, 10);
        List<String> strs1 = new Eql().limit(eqlPage).execute();
        TimeUnit.SECONDS.sleep(1);
        eqlPage.setTotalRows(0);
        List<String> strs2 = new Eql().limit(eqlPage).execute();

        assertThat(strs1, is(equalTo(strs2)));
        assertThat(eqlPage.getTotalRows(), is(11));
    }

    @BeforeClass
    public static void beforeClass() {
        MockDiamondServer.setUpMockServer();
    }

    @AfterClass
    public static void afterClass() {
        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void test4() {
        MockDiamondServer.setConfigInfo("EQL.CACHE", "org.n3r.eql.CacheTest.eql", "test4.cacheVersion=100");
        String str1 = new Eql().limit(1).execute();
        String str2 = new Eql().limit(1).execute();
        String str3 = new Eql().cached(false).limit(1).execute();
        MockDiamondServer.setConfigInfo("EQL.CACHE", "org.n3r.eql.CacheTest.eql", "test4.cacheVersion=200");
        String str4 = new Eql().limit(1).execute();

        assertThat(str1, is(str2));
        assertThat(str1, is(not(str3)));
        assertThat(str1, is(not(str4)));
    }
}
