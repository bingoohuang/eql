package org.n3r.eql.pojo;

import org.junit.Test;
import org.n3r.eql.Eql;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

// CREATE TABLE students (name VARCHAR(64), mark ENUM('ABSENT','COPY CASE' ));
// INSERT INTO students VALUES ('john', 'COPY CASE');
// SELECT * FROM students;
public class EnumMappingTest {
    @Test
    public void test1() {
        Custom value = new Eql("mysql").returnType(Custom.class).limit(1)
                .execute("select 'male' as sex, 1 as age");
        assertThat(value.sex, is(Sex.male));

        value = new Eql("mysql").returnType(Custom.class).limit(1)
                .execute("select 'female' as sex, 1 as age");
        assertThat(value.sex, is(Sex.female));
    }

    @Test
    public void test2() {
        Custom value = new Eql("mysql").returnType(Custom.class).limit(1)
                .execute("select 'male' as sex");
        assertThat(value.sex, is(Sex.male));

        value = new Eql("mysql").returnType(Custom.class).limit(1)
                .execute("select 'female' as sex");
        assertThat(value.sex, is(Sex.female));
    }


    @Test
    public void test3() {
        Sex value = new Eql("mysql").returnType(Sex.class).limit(1)
                .execute("select 'male' as sex");
        assertThat(value, is(Sex.male));

        value = new Eql("mysql").returnType(Sex.class).limit(1)
                .execute("select 'female' as sex");
        assertThat(value, is(Sex.female));
    }


    public static enum Sex {
        male,female
    }

    public static class Custom {
        Sex sex;
        int age;
    }
}


