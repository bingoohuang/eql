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

    @Test
    public void test4() {
        Sex2 value = new Eql("mysql").returnType(Sex2.class).limit(1)
                .execute("select '1' as sex");
        assertThat(value, is(Sex2.male));

        value = new Eql("mysql").returnType(Sex2.class).limit(1)
                .execute("select '0' as sex");
        assertThat(value, is(Sex2.female));
    }

    public static enum Sex {
        male,female
    }

    public static class Custom {
        Sex sex;
        int age;
    }

    public static enum Sex2 {
        male("1"),female("0");
        private final String value;

        Sex2(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Sex2 valueOff(String value) {
            for(Sex2 v : values())
                if(v.getValue().equalsIgnoreCase(value)) return v;
            throw new IllegalArgumentException();
        }
    }

    public static void main(String[] args) {
        System.out.println(Sex2.female);
    }

    public static class Custom2 {
        Sex2 sex;
        int age;
    }
}


