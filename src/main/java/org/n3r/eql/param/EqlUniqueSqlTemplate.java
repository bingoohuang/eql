package org.n3r.eql.param;


import lombok.AllArgsConstructor;
import lombok.Value;
import org.n3r.eql.impl.EqlUniqueSqlId;

@Value @AllArgsConstructor
public class EqlUniqueSqlTemplate {
    private EqlUniqueSqlId eqlUniqueSQLId;
    private String templateSQL;
}
