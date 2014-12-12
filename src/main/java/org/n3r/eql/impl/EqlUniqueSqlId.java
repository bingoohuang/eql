package org.n3r.eql.impl;

public class EqlUniqueSqlId {
    private String sqlClassPath, sqlId;

    public EqlUniqueSqlId(String sqlClassPath, String sqlId) {
        this.sqlClassPath = sqlClassPath;
        this.sqlId = sqlId;
    }

    public String getSqlClassPath() {
        return sqlClassPath;
    }

    public void setSqlClassPath(String sqlClassPath) {
        this.sqlClassPath = sqlClassPath;
    }

    public String getSqlId() {
        return sqlId;
    }

    public void setSqlId(String sqlId) {
        this.sqlId = sqlId;
    }

    @Override
    public String toString() {
        return "EqlUniquEQLId{" +
                "sqlClassPath='" + sqlClassPath + '\'' +
                ", sqlId='" + sqlId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EqlUniqueSqlId that = (EqlUniqueSqlId) o;

        if (!sqlClassPath.equals(that.sqlClassPath)) return false;
        if (!sqlId.equals(that.sqlId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sqlClassPath.hashCode();
        result = 31 * result + sqlId.hashCode();
        return result;
    }

    public EqlUniqueSqlId newTotalRowSqlId() {
        return new EqlUniqueSqlId(sqlClassPath, "__total_rows." + sqlId);
    }
}
