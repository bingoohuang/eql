package org.n3r.eql.implicit;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.Eql;
import org.n3r.eql.EqlContext;

import static com.google.common.truth.Truth.assertThat;

public class ImplicitTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql("h2").execute(
                "drop table if exists t_dpcode",
                "create table t_dpcode(name varchar(10), dpcode tinyint, remark varchar(100))");
    }

    @Test
    public void test1() {
        EqlContext.put("dpcode", 0);
        String a = new Eql("h2").limit(1).params("a", "b").execute();
        assertThat(a).isEqualTo("a");
    }

    @Test
    public void test2() {
        EqlContext.put("dpcode", 0);
        val map = ImmutableMap.of("a", "a", "b", "b");
        String a = new Eql("h2").limit(1).params(map).execute();
        assertThat(a).isEqualTo("a");
    }

    @Test
    public void test20() {
        EqlContext.put("dpcode", 100);
        val map = ImmutableMap.of("a", "a", "b", "b", "dpcode", "0");
        String a = new Eql("h2").limit(1).params(map).execute();
        assertThat(a).isEqualTo("a");


        val map2 = ImmutableMap.of("a", "a", "b", "b");
        String b = new Eql("h2").limit(1).params(map2).execute();
        assertThat(b).isNull();
    }

    @Test
    public void test3() {
        EqlContext.put("dpcode", 0);
        String a = new Eql("h2").limit(1).params("a", "b").execute();
        assertThat(a).isEqualTo("a");
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class DpCode {
        private String name;
        private int dpcode;
        private String remark;
    }

    @Test
    public void test4() {
        EqlContext.put("dpcode", 0);
        new Eql("h2").params("bingoo", "testbingoo").execute();

        DpCode dpCode = new Eql("h2").selectFirst("select").returnType(DpCode.class).execute();

        assertThat(dpCode).isEqualTo(new DpCode("bingoo", 0, "testbingoo"));

        val map = ImmutableMap.of("dpcode", 10);
        DpCode dpCode2 = new Eql("h2").selectFirst("select").params(map).returnType(DpCode.class).execute();
        assertThat(dpCode2).isNull();

        val map30 = ImmutableMap.<String, Object>of("name", "dingoo", "dpcode", 30, "remark", "xxx");
        new Eql("h2").update("test41").params(map30).execute();
        val map3 = ImmutableMap.of("dpcode", 30);
        DpCode dpCode3 = new Eql("h2").selectFirst("select").params(map3).returnType(DpCode.class).execute();
        assertThat(dpCode3).isEqualTo(new DpCode("dingoo", 30, "xxx"));

        val map40 = ImmutableMap.<String, Object>of("name", "pingoo", "dpcode", 40, "remark", "yyy");
        new Eql("h2").update("test42").params(map40).execute();
        val map4 = ImmutableMap.of("dpcode", 40);
        DpCode dpCode4 = new Eql("h2").selectFirst("select").params(map4).returnType(DpCode.class).execute();
        assertThat(dpCode4).isEqualTo(new DpCode("pingoo", 40, "yyy"));
    }

    @Test
    public void testEx1() {
        try {
            new Eql("h2").execute();
            Assert.fail();
        } catch (Exception ex) {
            assertThat(ex.getMessage()).contains("[testEx1] with different param binding types");
        }
    }

    @Test
    public void testEx2() {
        try {
            new Eql("h2").execute();
            Assert.fail();
        } catch (Exception ex) {
            assertThat(ex.getMessage()).contains("[testEx2] with different param binding types");
        }
    }

    @Test
    public void testEx3() {
        try {
            new Eql("h2").execute();
            Assert.fail();
        } catch (Exception ex) {
            assertThat(ex.getMessage()).contains("[testEx3] with different param binding types");
        }
    }
}
