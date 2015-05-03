package org.n3r.eql.eqler.dynamic;

import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.config.EqlConfigCache;
import org.n3r.eql.eqler.EqlerFactory;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DynamicEqlerTest {
    static DynamicEqler eqler;

    @BeforeClass
    public static void beforeClass() {
        eqler = EqlerFactory.getEqler(DynamicEqler.class);
    }

    @Test
    public void echo() {
        Map<String, String> echo = eqler.echo("bingoo", "huang");
        assertThat(echo.size(), is(2));
        assertThat(echo.get("hello"), is(equalTo("bingoo")));
        assertThat(echo.get("world"), is(equalTo("huang")));
    }

    @Test
    public void echoNamed() {
        Map<String, String> echo = eqler.echoNamed("bingoo", "huang");
        assertThat(echo.size(), is(2));
        assertThat(echo.get("hello"), is(equalTo("bingoo")));
        assertThat(echo.get("world"), is(equalTo("huang")));
    }

    @Test
    public void echoShared() {
        Map<String, String> echo = eqler.echoShared("bingoo", "huang");
        assertThat(echo.size(), is(3));
        assertThat(echo.get("hello"), is(equalTo("bingoo")));
        assertThat(echo.get("world"), is(equalTo("huang")));
        assertThat(echo.get("shared"), is(equalTo("bingoo")));
    }

    @Test
    public void echoShareNamed() {
        Map<String, String> echo = eqler.echoShareNamed("bingoo", "huang");
        assertThat(echo.size(), is(3));
        assertThat(echo.get("hello"), is(equalTo("bingoo")));
        assertThat(echo.get("world"), is(equalTo("huang")));
        assertThat(echo.get("shared"), is(equalTo("bingoo")));
    }

    @Test
    public void eqlConfig() {
        EqlConfig eqlConfig = EqlConfigCache.getEqlConfig("me");
        String echo = eqler.eqlConfig(eqlConfig);
        assertThat(echo, is(equalTo("abc")));
    }
}
