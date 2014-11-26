package org.n3r.eql.codedesc;

import org.junit.Test;
import org.n3r.eql.parser.OffsetAndOptionValue;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DescOptionValueParserTest {
    @Test
    public void test1() {
        DescOptionValueParser desc = new DescOptionValueParser();
        OffsetAndOptionValue oo = desc.parseValueOption("name@code1");
        assertThat(oo.getOffset(), is(10));
        assertThat(oo.getOptionValue(), is("name@code1"));
    }

    @Test
    public void test2() {
        DescOptionValueParser desc = new DescOptionValueParser();
        OffsetAndOptionValue oo = desc.parseValueOption("name@code1(abc)");
        assertThat(oo.getOffset(), is(15));
        assertThat(oo.getOptionValue(), is("name@code1(abc)"));
    }

    @Test
    public void test3() {
        DescOptionValueParser desc = new DescOptionValueParser();
        OffsetAndOptionValue oo = desc.parseValueOption("name@code1(abc, efg)");
        assertThat(oo.getOffset(), is(20));
        assertThat(oo.getOptionValue(), is("name@code1(abc, efg)"));
    }

    @Test
    public void test4() {
        DescOptionValueParser desc = new DescOptionValueParser();
        OffsetAndOptionValue oo = desc.parseValueOption("name@code1(abc, efg) key2");
        assertThat(oo.getOffset(), is(20));
        assertThat(oo.getOptionValue(), is("name@code1(abc, efg)"));
    }

    @Test
    public void test5() {
        DescOptionValueParser desc = new DescOptionValueParser();
        OffsetAndOptionValue oo = desc.parseValueOption(" name @ code1( abc, efg) key2");
        assertThat(oo.getOffset(), is(24));
        assertThat(oo.getOptionValue(), is("name @ code1( abc, efg)"));
    }
}
