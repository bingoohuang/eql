package org.n3r.eql.util;

import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class PairsParserTest {
    @Test
    public void test1() {
        Map<String, String> parse = new PairsParser().parse("key=value");
        assertThat(parse, equalTo((Map<String, String>) of("key", "value")));

    }

    @Test
    public void test2() {
        Map<String, String> parse = new PairsParser().parse(
                "item=columnDef collection=columnDefs key='order by'");
        assertThat(parse, equalTo((Map<String, String>) of("item", "columnDef",
                "collection", "columnDefs", "key", "order by")));
    }

    @Test
    public void test3() {
        Map<String, String> parse = new PairsParser().parse(
                "item=columnDef key='\\'order by'");
        assertThat(parse, equalTo((Map<String, String>) of("item", "columnDef",
                "key", "'order by")));
    }

    @Test
    public void test4() {
        Map<String, String> parse = new PairsParser().parse(
                "item=columnDef key='\"order by'");
        assertThat(parse, equalTo((Map<String, String>) of("item", "columnDef",
                "key", "\"order by")));
    }

    @Test
    public void test5() {
        Map<String, String> parse = new PairsParser().parse(
                "item=columnDef key=\"'order by\"");
        assertThat(parse, equalTo((Map<String, String>) of("item", "columnDef",
                "key", "'order by")));
    }

}
