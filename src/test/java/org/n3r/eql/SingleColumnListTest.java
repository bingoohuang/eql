package org.n3r.eql;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SingleColumnListTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql("mysql").execute();
    }

    @Test
    public void test1() {
        List<String> result = new Eql("mysql").execute();
        String first = result.get(0);
        assertThat(first, equalTo("10000"));
    }
}
