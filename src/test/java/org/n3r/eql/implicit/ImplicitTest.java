package org.n3r.eql.implicit;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
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

    }
}
