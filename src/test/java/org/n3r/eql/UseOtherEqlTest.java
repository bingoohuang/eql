package org.n3r.eql;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UseOtherEqlTest {
    @Test
    public void testUsEQLClass() {
        String str = new Eql()
                .selectFirst("test1")
                .useSqlFile(SimpleTest.class)
                .execute();
        assertThat(str, is("1"));
    }

    @Test
    public void testUsEQLFile() {
        String str = new Eql()
                .useSqlFile("org/n3r/eql/SimpleTest.eql")
                .selectFirst("test1").execute();
        assertThat(str, is("1"));
    }
}
