package org.n3r.eql.map;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EqlTypeTest {
    @Test
    public void test1() {
        EqlType eqlType = EqlType.parseSqlType("select * from dual");
        assertThat(eqlType, is(EqlType.SELECT));
    }

    @Test
    public void test2() {
        EqlType eqlType = EqlType.parseSqlType("/* SOME COMMENTS */select * from dual");
        assertThat(eqlType, is(EqlType.SELECT));
    }

    @Test
    public void test3() {
        EqlType eqlType = EqlType.parseSqlType("/*** result(1) ***/\nselect * from dual");
        assertThat(eqlType, is(EqlType.SELECT));
    }
}
