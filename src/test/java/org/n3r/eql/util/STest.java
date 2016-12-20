package org.n3r.eql.util;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/20.
 */
public class STest {
    @Test
    public void testUnQuote() {
        assertThat(S.unQuote(null, "'")).isNull();
        assertThat(S.unQuote("'", "'")).isEqualTo("'");
        assertThat(S.unQuote("''", "'")).isEqualTo("");
        assertThat(S.unQuote("'a'", "'")).isEqualTo("a");
        assertThat(S.unQuote("'a", "'")).isEqualTo("'a");
        assertThat(S.unQuote("a'", "'")).isEqualTo("a'");
        assertThat(S.unQuote("'ab'", "'")).isEqualTo("ab");
    }
}
