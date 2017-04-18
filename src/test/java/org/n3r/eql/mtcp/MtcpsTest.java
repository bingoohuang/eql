package org.n3r.eql.mtcp;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.n3r.eql.mtcp.utils.Mtcps;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MtcpsTest {
    @Test
    public void test1() {
        String urlTemplate = "jdbc:mysql://{host}:{port}/{dbname}?[useUnicode,characterEncoding,connectTimeout,autoReconnect]";
        Map<String, String> props = Maps.newHashMap();
        props.put("host", "localhost");
        props.put("port", "3306");
        props.put("dbname", "dba");

        assertThat(Mtcps.interpret(urlTemplate, props),
                is(equalTo("jdbc:mysql://localhost:3306/dba?")));
        assertThat(props.size(), is(0));
    }

    @Test(expected = RuntimeException.class)
    public void test1Ex() {
        String urlTemplate = "jdbc:mysql://{host}:{port}/{dbname}?[useUnicode,characterEncoding,connectTimeout,autoReconnect]";
        Map<String, String> props = Maps.newHashMap();
        props.put("host", "localhost");
        props.put("port", "3306");

        Mtcps.interpret(urlTemplate, props);
    }

    @Test
    public void test2() {
        String urlTemplate = "jdbc:mysql://{host}:{port}/{dbname}?[useUnicode,characterEncoding,connectTimeout,autoReconnect]";
        Map<String, String> props = Maps.newHashMap();
        props.put("host", "localhost");
        props.put("port", "3306");
        props.put("dbname", "dba");
        props.put("useUnicode", "true");

        assertThat(Mtcps.interpret(urlTemplate, props),
                is(equalTo("jdbc:mysql://localhost:3306/dba?useUnicode=true")));
        assertThat(props.size(), is(0));
    }

    @Test
    public void test3() {
        String urlTemplate = "jdbc:mysql://{host}:{port}/{dbname}?[useUnicode,characterEncoding,connectTimeout,autoReconnect]";
        Map<String, String> props = Maps.newHashMap();
        props.put("host", "localhost");
        props.put("port", "3306");
        props.put("dbname", "dba");
        props.put("useUnicode", "true");
        props.put("connectTimeout", "10000");

        assertThat(Mtcps.interpret(urlTemplate, props),
                is(equalTo("jdbc:mysql://localhost:3306/dba?useUnicode=true&connectTimeout=10000")));
        assertThat(props.size(), is(0));
    }

    @Test
    public void test4() {
        String urlTemplate = "jdbc:mysql://{host}:{port}/{dbname}?[useUnicode,characterEncoding,connectTimeout,autoReconnect],XXX";
        Map<String, String> props = Maps.newHashMap();
        props.put("host", "localhost");
        props.put("port", "3306");
        props.put("dbname", "dba");
        props.put("useUnicode", "true");
        props.put("connectTimeout", "10000");
        props.put("xxx", "yyy");

        assertThat(Mtcps.interpret(urlTemplate, props),
                is(equalTo("jdbc:mysql://localhost:3306/dba?useUnicode=true&connectTimeout=10000,XXX")));
        assertThat(props.size(), is(1));
    }
}
