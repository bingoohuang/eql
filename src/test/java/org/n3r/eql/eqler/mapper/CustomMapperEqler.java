package org.n3r.eql.eqler.mapper;

import org.n3r.eql.eqler.annotations.*;
import org.n3r.eql.map.CodeValueMapper;
import org.n3r.eql.map.EqlRowMapper;

import java.util.List;
import java.util.Map;

@EqlerConfig("mysql")
public interface CustomMapperEqler {
    @EqlMapper(CodeValueMapper.class)
    @Sql("select 'name' as code, 'bingoo' as value " +
            "union all select 'age' as code, '123' as value")
    Map<String, String> queryParams1();

    Map<String, String> queryParams2(EqlRowMapper eqlRowMapper);

    @SqlId("queryParams2")
    <T> List<T> queryParam3(@ReturnType Class<T> returnTypeClass);

    @Sql("select 'name' as code, 'bingoo' as value ")
    <T> T queryParam4(@ReturnType Class<T> returnTypeClass);

    @Sql("select 'name' as code, 'bingoo' as value ")
    <T> T queryParam4(long id, @ReturnType Class<T> returnTypeClass);
}
