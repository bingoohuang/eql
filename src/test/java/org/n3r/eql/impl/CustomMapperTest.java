package org.n3r.eql.impl;

import org.junit.Before;
import org.junit.Test;
import org.n3r.eql.Eql;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CustomMapperTest {
    @Before
    public void before() {
        new Eql("mysql").execute();
    }

    @Test
    public void test1() {
        DecodeMapper myMapper = new DecodeMapper(
                "A000014", "brandCode", "brandName",
                "A000015", "modelCode", "modelName",
                "A000016", "colorCode", "colorName"
        );

        new Eql("mysql").returnType(myMapper).execute();
        Map<String, String> map = myMapper.getMap();

        assertThat(map.get("brandCode"), is(equalTo("A000014")));
        assertThat(map.get("brandName"), is(equalTo("三星")));

        assertThat(map.get("modelCode"), is(equalTo("A000015")));
        assertThat(map.get("modelName"), is(equalTo("NOTE")));

        assertThat(map.get("colorCode"), is(equalTo("A000016")));
        assertThat(map.get("colorName"), is(equalTo("金色")));
    }
}
