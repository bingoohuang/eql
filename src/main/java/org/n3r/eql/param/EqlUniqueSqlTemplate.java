package org.n3r.eql.param;


import org.n3r.eql.impl.EqlUniqueSqlId;

public class EqlUniqueSqlTemplate {
    private EqlUniqueSqlId eqlUniquEQLId;
    private String templatEQL;

    public EqlUniqueSqlTemplate(EqlUniqueSqlId eqlUniquEQLId, String templatEQL) {
        this.eqlUniquEQLId = eqlUniquEQLId;
        this.templatEQL = templatEQL;
    }

    public EqlUniqueSqlId getEqlUniquEQLId() {
        return eqlUniquEQLId;
    }

    public void setEqlUniquEQLId(EqlUniqueSqlId eqlUniquEQLId) {
        this.eqlUniquEQLId = eqlUniquEQLId;
    }

    public String getTemplatEQL() {
        return templatEQL;
    }

    public void setTemplatEQL(String templatEQL) {
        this.templatEQL = templatEQL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EqlUniqueSqlTemplate that = (EqlUniqueSqlTemplate) o;

        if (eqlUniquEQLId != null ? !eqlUniquEQLId.equals(that.eqlUniquEQLId) : that.eqlUniquEQLId != null)
            return false;
        if (templatEQL != null ? !templatEQL.equals(that.templatEQL) : that.templatEQL != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = eqlUniquEQLId != null ? eqlUniquEQLId.hashCode() : 0;
        result = 31 * result + (templatEQL != null ? templatEQL.hashCode() : 0);
        return result;
    }
}
