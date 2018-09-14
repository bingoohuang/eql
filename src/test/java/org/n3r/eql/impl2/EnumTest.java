package org.n3r.eql.impl2;

import lombok.val;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.Eql;
import org.n3r.eql.pojo.EnumMappingTest;

import static com.google.common.truth.Truth.assertThat;

public class EnumTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql("h2").execute(
                "DROP TABLE IF EXISTS T_EnumTest",
                "CREATE TABLE T_EnumTest (ID INT, NAME VARCHAR(10))");
    }

    @Test
    public void test() {
        new Eql("h2")
                .params(100, EnumMappingTest.Sex.male)
                .execute(
                "insert into T_EnumTest(id, name)" +
                "values (##, ##)");

        val name = new Eql("h2").params(100)
                .returnType(String.class)
                .limit(1)
                .execute("select name from T_EnumTest where id = ##");
        assertThat(name).isEqualTo("male");

        EnumMappingTest.Custom custom = new EnumMappingTest.Custom();
        custom.setAge(200);
        custom.setSex(EnumMappingTest.Sex.female);

        new Eql("h2")
                .params(custom)
                .execute(
                        "insert into T_EnumTest(id, name) values (#age#, #sex#)");

        EnumMappingTest.Custom cu = new Eql("h2").params(200)
                .returnType(EnumMappingTest.Custom.class)
                .limit(1)
                .execute("select id as age, name as sex from T_EnumTest where id = ##");
        assertThat(cu).isEqualTo(custom);
    }

}
