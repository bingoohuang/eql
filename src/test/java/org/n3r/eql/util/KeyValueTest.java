package org.n3r.eql.util;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class KeyValueTest {
    @Test
    public void test1() {
        assertThat(KeyValue.parse("key=value"), is(new KeyValue("key", "value")));
        assertThat(KeyValue.parse("key="), is(new KeyValue("key", "")));
        assertThat(KeyValue.parse("key"), is(new KeyValue("key", "")));


        assertThat(KeyValue.parse("prefix.key=value").removeKeyPrefix("prefix"), is(new KeyValue("key", "value")));
        assertThat(KeyValue.parse("prefix.key=value").removeKeyPrefix("prefix."), is(new KeyValue("key", "value")));


    }

    @Test(expected = RuntimeException.class)
    public void test2() {
        KeyValue.parse("=key");
    }
}
