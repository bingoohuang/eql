package org.n3r.eql.impl2;

import lombok.val;
import org.junit.Test;
import org.n3r.eql.Eql;

import static com.google.common.truth.Truth.assertThat;

public class UnpooledTest {
    @Test
    public void test() {
        val abc = new Eql("unpooled").limit(1).execute("SELECT 'ABC'");
        assertThat(abc).isEqualTo("ABC");
    }
}
