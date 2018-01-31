package org.n3r.eql.util;

import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

public class MapInvocationHandlerTest {
    @Test
    public void test1() {
        HashMap<String, Object> map = Maps.newHashMap();
        map.put("table", "abcd");

        HashMap<String, Object> context = Maps.newHashMap();
        Map<String, Object> delayedBean = MapInvocationHandler.proxy(context, map);
        Object table = delayedBean.get("table");
        assertThat(table).isEqualTo("abcd");
    }
}
