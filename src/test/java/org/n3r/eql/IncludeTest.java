package org.n3r.eql;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.sql.Timestamp;

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
    }
}
