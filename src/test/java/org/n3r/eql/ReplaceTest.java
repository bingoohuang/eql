package org.n3r.eql;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.sql.Timestamp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ReplaceTest {
    @Test
    public void test1() {
        new Eql().id("dropTestTable").execute();
        new Eql().id("createTestTable").params(new Timestamp(1383122146000l)).execute();

        String str = new Eql().selectFirst("replace1").params("x").dynamics("DUAL").execute();
        assertThat(str, is("x"));

        str = new Eql().selectFirst("replace2").params("x")
                .dynamics(ImmutableMap.of("table", "DUAL")).execute();
        assertThat(str, is("x"));
    }
}
