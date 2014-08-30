package org.n3r.eql.app;

import org.junit.Test;
import org.n3r.eql.joor.Reflect;
import org.n3r.eql.util.Ob;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class ObTest {
    @Test
    public void test() {
        Object o = Ob.createInstance(Obj.class);
        assertThat(o, is(notNullValue()));

        o = Reflect.on(Obj.class).create().get();
        assertThat(o, is(notNullValue()));
    }

    class Obj {
        private String name;

        Obj(String name) {
            this.name = name;
        }
    }
}
