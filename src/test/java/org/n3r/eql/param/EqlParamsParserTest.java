package org.n3r.eql.param;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class EqlParamsParserTest {

    @Test
    public void unquote() {
        assertThat(EqlParamsParser.unquote("abc")).isEqualTo("abc");
        assertThat(EqlParamsParser.unquote("`abc`")).isEqualTo("abc");
        assertThat(EqlParamsParser.unquote("'abc'")).isEqualTo("abc");
        assertThat(EqlParamsParser.unquote("'abc\"")).isEqualTo("'abc\"");
    }
}