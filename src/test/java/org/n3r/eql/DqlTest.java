package org.n3r.eql;

import org.junit.Test;
import org.n3r.eql.diamond.Dql;

import static org.junit.Assert.assertEquals;

public class DqlTest {
    @Test
    public void test() {
        int result = new Dql().selectFirst("demo").execute();

        assertEquals(1, result);
    }
}
