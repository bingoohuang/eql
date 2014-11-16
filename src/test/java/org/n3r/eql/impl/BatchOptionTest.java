package org.n3r.eql.impl;

import com.google.common.collect.Lists;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.Eql;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BatchOptionTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql("mysql").execute();
    }

    @Test
    public void insert() {
        List<IdName> idNames = Lists.newArrayList(
                new IdName(1, "bingoo"),
                new IdName(2, "dingoo"),
                new IdName(3, "pingoo"),
                new IdName(4, "pingoo"));

        new Eql("mysql").id("insert").params(idNames).execute();

        List<IdName> idNamesReturn = new Eql("mysql").id("select").returnType(IdName.class).execute();
        assertThat(idNamesReturn, is(equalTo(idNames)));

        idNames = Lists.newArrayList(
                new IdName(1, "bingoo huang"),
                new IdName(2, "dingoo huang"),
                new IdName(3, "pingoo huang"),
                new IdName(4, "pingoo huang"));


        int rows = new Eql("mysql").id("update").params(idNames).execute();
        assertThat(rows, is(4));

        rows = new Eql("mysql").id("delete").params(idNames).execute();
        assertThat(rows, is(4));

        idNamesReturn = new Eql("mysql").id("select").returnType(IdName.class).execute();
        assertThat(idNamesReturn.size(), is(equalTo(0)));
    }

    public static class IdName {
        private int id;
        private String name;

        public IdName(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public IdName() {
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IdName idName = (IdName) o;

            if (id != idName.id) return false;
            if (name != null ? !name.equals(idName.name) : idName.name != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }
    }
}
