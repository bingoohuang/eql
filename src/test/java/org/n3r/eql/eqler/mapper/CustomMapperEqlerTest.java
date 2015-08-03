package org.n3r.eql.eqler.mapper;

import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.eqler.EqlerFactory;
import org.n3r.eql.map.CodeValueMapper;
import org.n3r.eql.map.EqlRowMapper;

import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

public class CustomMapperEqlerTest {
    static CustomMapperEqler eqler;

    @BeforeClass
    public static void beforeClass() {
        eqler = EqlerFactory.getEqler(CustomMapperEqler.class);
    }

    @Test
    public void annotationMapper() {
        Map<String, String> map = eqler.queryParams1();
        assertThat(map.get("name")).isEqualTo("bingoo");
        assertThat(map.get("age")).isEqualTo("123");
        assertThat(map.size()).isEqualTo(2);
    }

    @Test
    public void parameterMapper() {
        EqlRowMapper mapper = new CodeValueMapper();
        Map<String, String> map = eqler.queryParams2(mapper);
        assertThat(map.get("name")).isEqualTo("huang");
        assertThat(map.get("age")).isEqualTo("321");
        assertThat(map.size()).isEqualTo(2);
    }

    @Test
    public void parameterMapperClass() {
        List<MyRow> rows = eqler.queryParam3(MyRow.class);
        assertThat(rows.size()).isEqualTo(2);
        assertThat(rows.get(0).getCode()).isEqualTo("name");
        assertThat(rows.get(0).getValue()).isEqualTo("huang");
        assertThat(rows.get(1).getCode()).isEqualTo("age");
        assertThat(rows.get(1).getValue()).isEqualTo("321");
    }
}
