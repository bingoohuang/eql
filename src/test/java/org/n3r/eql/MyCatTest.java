package org.n3r.eql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.n3r.eql.util.EqlUtils;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class MyCatTest {
    @Test
    public void test1() {
        long rows = new Eql("mycat").selectFirst("test").params("100601101001402181545421590002").execute();
        assertEquals(rows, 1L);
    }

    @Test
    public void testMaxIn() {
        Map<String, Object> map = Maps.newHashMap();
        List<String> list = EqlUtils.classResourceToLines("order_no1.txt");
        map.put("list", list);

        long rows = new Eql("mycat").selectFirst("testMaxIn").params(map).execute();
        assertTrue(list.size() >= rows);
    }
}
