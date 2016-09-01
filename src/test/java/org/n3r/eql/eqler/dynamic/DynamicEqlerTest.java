package org.n3r.eql.eqler.dynamic;

import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.config.EqlConfigCache;
import org.n3r.eql.eqler.EqlerFactory;

import java.util.Map;

import static com.google.common.truth.Truth.assertThat;


public class DynamicEqlerTest {
    static DynamicEqler eqler;

    @BeforeClass
    public static void beforeClass() {
        eqler = EqlerFactory.getEqler(DynamicEqler.class);
    }

    @Test
    public void echo() {
        Map<String, String> echo = eqler.echo("bingoo", "huang");
        assertThat(echo.size()).isEqualTo(2);
        assertThat(echo.get("hello")).isEqualTo("bingoo");
        assertThat(echo.get("world")).isEqualTo("huang");
    }

    @Test
    public void echoNamed() {
        Map<String, String> echo = eqler.echoNamed("bingoo", "huang", "echoNamed");
        assertThat(echo.size()).isEqualTo(2);
        assertThat(echo.get("hello")).isEqualTo("bingoo");
        assertThat(echo.get("world")).isEqualTo("huang");
    }

    @Test
    public void echoNamedWithSqlId() {
        Map<String, String> echo = eqler.echoNamedWithSqlId("bingoo", "huang", "echoNamed");
        assertThat(echo.size()).isEqualTo(2);
        assertThat(echo.get("hello1")).isEqualTo("bingoo");
        assertThat(echo.get("world1")).isEqualTo("huang");
    }

    @Test
    public void echoShared() {
        Map<String, String> echo = eqler.echoShared("bingoo", "huang");
        assertThat(echo.size()).isEqualTo(3);
        assertThat(echo.get("hello")).isEqualTo("bingoo");
        assertThat(echo.get("world")).isEqualTo("huang");
        assertThat(echo.get("shared")).isEqualTo("bingoo");
    }

    @Test
    public void echoShareNamed() {
        Map<String, String> echo = eqler.echoShareNamed("bingoo", "huang");
        assertThat(echo.size()).isEqualTo(3);
        assertThat(echo.get("hello")).isEqualTo("bingoo");
        assertThat(echo.get("world")).isEqualTo("huang");
        assertThat(echo.get("shared")).isEqualTo("bingoo");
    }

    @Test
    public void eqlConfig() {
        EqlConfig eqlConfig = EqlConfigCache.getEqlConfig("me");
        String echo = eqler.eqlConfig(eqlConfig);
        assertThat(echo).isEqualTo("abc");
    }
}
