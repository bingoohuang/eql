package org.n3r.eql.parser;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class WordOptionValueParserTest {
    @Test
    public void test1() {
        WordOptionValueParser parser = new WordOptionValueParser();
        OffsetAndOptionValue oo = parser.parseValueOption("abc");
        assertThat(oo.getOffset(), is(3));
        assertThat(oo.getOptionValue(), is(equalTo("abc")));
    }

    @Test
    public void test11() {
        WordOptionValueParser parser = new WordOptionValueParser();
        OffsetAndOptionValue oo = parser.parseValueOption("  abc");
        assertThat(oo.getOffset(), is(5));
        assertThat(oo.getOptionValue(), is(equalTo("abc")));
    }

    @Test
    public void test12() {
        WordOptionValueParser parser = new WordOptionValueParser();
        OffsetAndOptionValue oo = parser.parseValueOption("  abc efg");
        assertThat(oo.getOffset(), is(5));
        assertThat(oo.getOptionValue(), is(equalTo("abc")));
    }

    @Test
    public void test2() {
        WordOptionValueParser parser = new WordOptionValueParser();
        OffsetAndOptionValue oo = parser.parseValueOption("\"abc\"");
        assertThat(oo.getOffset(), is(5));
        assertThat(oo.getOptionValue(), is(equalTo("abc")));
    }

    @Test
    public void test21() {
        WordOptionValueParser parser = new WordOptionValueParser();
        OffsetAndOptionValue oo = parser.parseValueOption("  \"abc\"");
        assertThat(oo.getOffset(), is(7));
        assertThat(oo.getOptionValue(), is(equalTo("abc")));
    }


    @Test
    public void test3() {
        WordOptionValueParser parser = new WordOptionValueParser();
        OffsetAndOptionValue oo = parser.parseValueOption("'abc'");
        assertThat(oo.getOffset(), is(5));
        assertThat(oo.getOptionValue(), is(equalTo("abc")));
    }
}
