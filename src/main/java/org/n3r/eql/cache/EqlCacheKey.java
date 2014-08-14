package org.n3r.eql.cache;


import org.n3r.eql.EqlPage;
import org.n3r.eql.impl.EqlUniqueSqlId;

import java.util.Arrays;

public class EqlCacheKey {
    private EqlUniqueSqlId uniqueSqlId;
    private Object[] params;
    private Object[] dynamics;
    private EqlPage page;

    public EqlCacheKey(EqlUniqueSqlId uniqueSqlId, Object[] params, Object[] dynamics, EqlPage page) {
        this.uniqueSqlId = uniqueSqlId;
        this.params = params;
        this.dynamics = dynamics;
        this.page = page;
    }

    public EqlUniqueSqlId getUniqueSqlId() {
        return uniqueSqlId;
    }

    public Object[] getParams() {
        return params;
    }

    public Object[] getDynamics() {
        return dynamics;
    }

    public EqlPage getPage() {
        return page;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EqlCacheKey that = (EqlCacheKey) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(dynamics, that.dynamics)) return false;
        if (page != null ? !page.equals(that.page) : that.page != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(params, that.params)) return false;
        if (uniqueSqlId != null ? !uniqueSqlId.equals(that.uniqueSqlId) : that.uniqueSqlId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uniqueSqlId != null ? uniqueSqlId.hashCode() : 0;
        result = 31 * result + (params != null ? Arrays.hashCode(params) : 0);
        result = 31 * result + (dynamics != null ? Arrays.hashCode(dynamics) : 0);
        result = 31 * result + (page != null ? page.hashCode() : 0);
        return result;
    }
}
