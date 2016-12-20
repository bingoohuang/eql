package org.n3r.eql.cache;


import lombok.Value;
import org.n3r.eql.EqlPage;
import org.n3r.eql.impl.EqlUniqueSqlId;

@Value
public class EqlCacheKey {
    private EqlUniqueSqlId uniqueSQLId;
    private Object[] params;
    private Object[] dynamics;
    private EqlPage page;
}
