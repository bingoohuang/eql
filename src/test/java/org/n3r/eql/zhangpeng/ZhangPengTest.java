package org.n3r.eql.zhangpeng;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.Eql;

import static com.google.common.truth.Truth.assertThat;

/**
 * Created by bingoohuang on 2017/3/22.
 */
public class ZhangPengTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql("h2").execute(
                "drop table if exists zhangpeng",
                "create table zhangpeng(first_name varchar(10), last_name varchar(10))",
                "insert into zhangpeng(first_name, last_name) values('zhang', 'peng')");
    }

    @Test
    public void mapping1() {
        val zhangPeng = new Eql("h2").limit(1).returnType(ZhangPeng.class)
                .execute("select first_name, last_name from zhangpeng");
        assertThat(zhangPeng).isEqualTo(new ZhangPeng("zhang", "peng"));
    }


    @Test
    public void mapping2() {
        val zhangPeng = new Eql("h2").limit(1).returnType(ZhangPeng2.class)
                .execute("select first_name, last_name from zhangpeng");
        assertThat(zhangPeng).isEqualTo(new ZhangPeng2("zhang", "peng"));
    }

    @Test
    public void returnInteger() {
        Integer intValue = new Eql("h2").limit(1)
                .returnType(Integer.class).execute("select 1 where 2 > 3");
        assertThat(intValue).isNull();
    }

    @Test(expected = NullPointerException.class)
    public void returnInt() {
        int intValue = new Eql("h2").limit(1)
                .returnType(int.class).execute("select 1 where 2 > 3");
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ZhangPeng {
        private String first_name;
        private String last_name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ZhangPeng2 {
        private String firstName;
        private String lastName;
    }
}
