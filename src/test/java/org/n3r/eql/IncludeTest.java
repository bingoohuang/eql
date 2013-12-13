package org.n3r.eql;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.sql.Timestamp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IncludeTest {
    @Test
    public void testInclude() {
        Eqll.choose("mysql");
        new Eqll().id("dropTestTable").execute();
        new Eqll().id("createTestTable").params(new Timestamp(System.currentTimeMillis())).execute();

        new Eqll().id("include")
                .params(ImmutableMap.builder()
                        .put("c1", "ccc111").put("a1", 3)
                        .put("c2", "ccc222").put("a2", 4).build())
        .execute();


        String c3 = new Eqll().selectFirst("selectC").params(3).execute();
        assertThat(c3, is("ccc111"));

        String c4 = new Eqll().selectFirst("selectC").params(4).execute();
        assertThat(c4, is("ccc222"));
    }
}
