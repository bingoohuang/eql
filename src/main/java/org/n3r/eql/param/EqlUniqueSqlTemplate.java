package org.n3r.eql.param;


import org.n3r.eql.impl.EqlUniqueSqlId;

public class EqlUniqueSqlTemplate {
    private EqlUniqueSqlId eqlUniqueSqlId;
    private String templateSql;

    public EqlUniqueSqlTemplate(EqlUniqueSqlId eqlUniqueSqlId, String templateSql) {
        this.eqlUniqueSqlId = eqlUniqueSqlId;
        this.templateSql = templateSql;
    }

    public EqlUniqueSqlId getEqlUniqueSqlId() {
        return eqlUniqueSqlId;
    }

    public void setEqlUniqueSqlId(EqlUniqueSqlId eqlUniqueSqlId) {
        this.eqlUniqueSqlId = eqlUniqueSqlId;
    }

    public String getTemplateSql() {
        return templateSql;
    }

    public void setTemplateSql(String templateSql) {
        this.templateSql = templateSql;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EqlUniqueSqlTemplate that = (EqlUniqueSqlTemplate) o;

        if (eqlUniqueSqlId != null ? !eqlUniqueSqlId.equals(that.eqlUniqueSqlId) : that.eqlUniqueSqlId != null)
            return false;
        if (templateSql != null ? !templateSql.equals(that.templateSql) : that.templateSql != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = eqlUniqueSqlId != null ? eqlUniqueSqlId.hashCode() : 0;
        result = 31 * result + (templateSql != null ? templateSql.hashCode() : 0);
        return result;
    }
}
