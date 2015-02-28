package org.n3r.eql.impl;

import org.junit.Test;
import org.n3r.eql.Eql;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BooleanTest {
    @Test
    public void test() {
        new Eql("mysql").execute("drop table if exists eql_boolean");
        new Eql("mysql").execute("create table eql_boolean(id tinyint(1) primary key, found tinyint(1) default 0)");
        new Eql("mysql").params(true).execute("insert into eql_boolean values(1, ##)");
        new Eql("mysql").params(false).execute("insert into eql_boolean values(0, ##)");
        boolean found;

        found = new Eql("mysql").returnType(boolean.class).limit(1).execute("select found from eql_boolean where id = 1");
        assertThat(found, is(true));

        found = new Eql("mysql").returnType(boolean.class).limit(1).execute("select found from eql_boolean where id = 0");
        assertThat(found, is(false));

        found = new Eql("mysql").returnType(Boolean.class).limit(1).execute("select found from eql_boolean where id = 1");
        assertThat(found, is(true));

        found = new Eql("mysql").returnType(Boolean.class).limit(1).execute("select found from eql_boolean where id = 0");
        assertThat(found, is(false));

        BooleanObject booleanObject;

        booleanObject = new Eql("mysql").returnType(BooleanObject.class).limit(1).execute("select id, found from eql_boolean where id = 1");
        assertThat(booleanObject.toString(), is(equalTo("{id=1, found=true}")));
        booleanObject = new Eql("mysql").returnType(BooleanObject.class).limit(1).execute("select id, found from eql_boolean where id = 0");
        assertThat(booleanObject.toString(), is(equalTo("{id=0, found=false}")));

        List<BooleanObject> booleanObjects = new Eql("mysql").returnType(BooleanObject.class).execute("select id, found from eql_boolean order by id");
        assertThat(booleanObjects.toString(), is(equalTo("[{id=0, found=false}, {id=1, found=true}]")));

        BooleanObject2 booleanObject2;

        booleanObject2 = new Eql("mysql").returnType(BooleanObject2.class).limit(1).execute("select id, found from eql_boolean where id = 1");
        assertThat(booleanObject2.toString(), is(equalTo("{id=1, found=true}")));
        booleanObject2 = new Eql("mysql").returnType(BooleanObject2.class).limit(1).execute("select id, found from eql_boolean where id = 0");
        assertThat(booleanObject2.toString(), is(equalTo("{id=0, found=false}")));

        List<BooleanObject2> booleanObject2s = new Eql("mysql").returnType(BooleanObject2.class).execute("select id, found from eql_boolean order by id");
        assertThat(booleanObject2s.toString(), is(equalTo("[{id=0, found=false}, {id=1, found=true}]")));
    }

    public static class BooleanObject {
        private int id;
        private boolean found;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public boolean isFound() {
            return found;
        }

        public void setFound(boolean found) {
            this.found = found;
        }

        @Override
        public String toString() {
            return "{" +
                    "id=" + id +
                    ", found=" + found +
                    '}';
        }
    }

    public static class BooleanObject2 {
        private int id;
        private Boolean found;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public Boolean isFound() {
            return found;
        }

        public void setFound(Boolean found) {
            this.found = found;
        }

        @Override
        public String toString() {
            return "{" +
                    "id=" + id +
                    ", found=" + found +
                    '}';
        }
    }
}
