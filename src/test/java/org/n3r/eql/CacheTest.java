package org.n3r.eql;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
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
        TimeUnit.SECONDS.sleep(3);
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
}
