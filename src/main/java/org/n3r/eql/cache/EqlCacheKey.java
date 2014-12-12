package org.n3r.eql.cache;


import org.n3r.eql.EqlPage;
import org.n3r.eql.impl.EqlUniqueSqlId;

import java.util.Arrays;

public class EqlCacheKey {
    private EqlUniqueSqlId uniquEQLId;
    private Object[] params;
    private Object[] dynamics;
    private EqlPage page;

    public EqlCacheKey(EqlUniqueSqlId uniquEQLId, Object[] params, Object[] dynamics, EqlPage page) {
        this.uniquEQLId = uniquEQLId;
        this.params = params;
        this.dynamics = dynamics;
        this.page = page;
    }

    public EqlUniqueSqlId getUniquEQLId() {
        return uniquEQLId;
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
        if (uniquEQLId != null ? !uniquEQLId.equals(that.uniquEQLId) : that.uniquEQLId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uniquEQLId != null ? uniquEQLId.hashCode() : 0;
        result = 31 * result + (params != null ? Arrays.hashCode(params) : 0);
        result = 31 * result + (dynamics != null ? Arrays.hashCode(dynamics) : 0);
        result = 31 * result + (page != null ? page.hashCode() : 0);
        return result;
    }
}
