package org.n3r.eql.eqler.annotations;

import org.n3r.eql.map.EqlRowMapper;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EqlMapper {
    Class<? extends EqlRowMapper> value();
}
