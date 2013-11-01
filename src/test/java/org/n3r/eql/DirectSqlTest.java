package org.n3r.eql;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class DirectSqlTest {
    @Test
    public void testDirectSql1() {
        String str = new Eql().limit(1).execute("select 'xx' from DUAL");
        assertThat(str, is("xx"));
    }

    @Test
    public void testDirectSql2() {
        String str = new Eql().limit(1).params("x")
                .execute("select 'xx' from DUAL where 'x' = ##");
        assertThat(str, is("xx"));

        str = new Eql().limit(1).params("y")
                .execute("select 'xx' from DUAL where 'x' = ##");
        assertThat(str, is(nullValue()));
    }
}
