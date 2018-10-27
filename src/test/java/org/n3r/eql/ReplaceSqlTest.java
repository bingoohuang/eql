package org.n3r.eql;

import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Timestamp;

import static com.google.common.collect.ImmutableMap.of;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ReplaceSqlTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql("mysql").execute();
    }

    @Test
    public void test1() {
        int rows = new Eql("mysql")
                .params(1, "Old", new Timestamp(System.currentTimeMillis()))
                .execute();
        assertThat(rows, is(equalTo(1)));

        rows = new Eql("mysql")
                .params(1, "New", new Timestamp(System.currentTimeMillis() + 1000))
                .execute();
        assertThat(rows, is(equalTo(2)));
    }

    @Test
    public void test2() {
        int rows = new Eql("mysql")
                .params(of("id", 2, "data", "Old", "ts", new Timestamp(System.currentTimeMillis())))
                .execute();
        assertThat(rows, is(equalTo(1)));

        rows = new Eql("mysql")
                .params(of("id", 2, "data", "New", "ts", new Timestamp(System.currentTimeMillis())))
                .execute();
        assertThat(rows, is(equalTo(2)));
    }

    @Test
    public void test3() {
        int rows = new Eql("mysql")
                .params(of("id", 3, "data", "Old", "ts", new Timestamp(System.currentTimeMillis())))
                .execute();
        assertThat(rows, is(equalTo(1)));

        rows = new Eql("mysql")
                .params(of("id", 3, "data", "New", "ts", new Timestamp(System.currentTimeMillis())))
                .execute();
        assertThat(rows, is(equalTo(2)));
    }
}
