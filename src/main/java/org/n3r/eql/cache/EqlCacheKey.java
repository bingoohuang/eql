package org.n3r.eql.cache;


import org.n3r.eql.impl.EqlUniqueSqlId;

import java.util.Arrays;

public class EqlCacheKey {
    private EqlUniqueSqlId uniqueSqlId;
    private Object[] params;
    private Object[] dynamics;

    public EqlCacheKey(EqlUniqueSqlId uniqueSqlId, Object[] params, Object[] dynamics) {
        this.uniqueSqlId = uniqueSqlId;
        this.params = params;
        this.dynamics = dynamics;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EqlCacheKey that = (EqlCacheKey) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(dynamics, that.dynamics)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(params, that.params)) return false;
        if (!uniqueSqlId.equals(that.uniqueSqlId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uniqueSqlId.hashCode();
        result = 31 * result + (params != null ? Arrays.hashCode(params) : 0);
        result = 31 * result + (dynamics != null ? Arrays.hashCode(dynamics) : 0);
        return result;
    }
}
