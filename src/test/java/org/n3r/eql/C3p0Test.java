package org.n3r.eql;

import org.junit.Test;

public class C3p0Test {
    @Test
    public void test() {
        new Eql("c3p0").update("test").execute();
        new Eql("c3p0").update("data").params("abc").execute();
    }
}
