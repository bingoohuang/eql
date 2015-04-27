package org.n3r.eql.eqler.spring;

import org.n3r.eql.eqler.annotations.EqlConfig;
import org.n3r.eql.eqler.annotations.Eqler;

@Eqler
@EqlConfig("mysql")
public interface SpEqler {
    int queryOne();
}
