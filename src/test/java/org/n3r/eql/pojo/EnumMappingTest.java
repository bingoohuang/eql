package org.n3r.eql.pojo;

import org.junit.Test;
import org.n3r.eql.Eql;
import org.n3r.eql.param.InternalValueable;

import static org.hamcrest.CoreMatchers.equalTo;
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

    @Test
    public void testEnumAsParam() {
        new Eql("mysql").execute("drop table if exists eql_sex", "create table eql_sex(sex char(1))");
        new Eql("mysql").params(Sex2.male).execute("insert into eql_sex value(##)");
        Eql eql = new Eql("mysql").limit(1).returnType(Sex2.class).params(Sex2.male);
        Sex2 value = eql.execute("select sex from eql_sex where sex = ##");
        assertThat(value, is(Sex2.male));
        assertThat(eql.getEqlRun().getEvalSql(), is(equalTo("select sex from eql_sex where sex = '1'")));
    }

    public enum Sex {
        male,female
    }

    public static class Custom {
        Sex sex;
        int age;
    }

    public enum Sex2 implements InternalValueable<String> {
        male("1"),female("0");
        private final String value;

        Sex2(String value) {
            this.value = value;
        }

        @Override
        public String internalValue() {
            return value;
        }

        public static Sex2 valueOff(String value) {
            for(Sex2 v : values())
                if(v.internalValue().equalsIgnoreCase(value)) return v;
            throw new IllegalArgumentException();
        }
    }

    public static class Custom2 {
        Sex2 sex;
        int age;
    }
}


