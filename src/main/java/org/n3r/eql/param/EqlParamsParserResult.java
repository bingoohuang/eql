package org.n3r.eql.param;


import org.n3r.eql.map.EqlRun;

public class EqlParamsParserResult {
    private EqlRun.EqlType sqlType;
    private String runSql;
    private int placeholderNum;
    private PlaceholderType placeHolderType;
    private PlaceholderType placeHolderOutType;
    private EqlParamPlaceholder[] placeHolders;

    public void setSqlType(EqlRun.EqlType sqlType) {
        this.sqlType = sqlType;
    }

    public EqlRun.EqlType getSqlType() {
        return sqlType;
    }

    public void setRunSql(String runSql) {
        this.runSql = runSql;
    }

    public String getRunSql() {
        return runSql;
    }

    public void setPlaceholderNum(int placeholderNum) {
        this.placeholderNum = placeholderNum;
    }

    public int getPlaceholderNum() {
        return placeholderNum;
    }

    public void setPlaceHolderType(PlaceholderType placeHolderType) {
        this.placeHolderType = placeHolderType;
    }

    public PlaceholderType getPlaceHolderType() {
        return placeHolderType;
    }

    public void setPlaceHolderOutType(PlaceholderType placeHolderOutType) {
        this.placeHolderOutType = placeHolderOutType;
    }

    public PlaceholderType getPlaceHolderOutType() {
        return placeHolderOutType;
    }

    public void setPlaceHolders(EqlParamPlaceholder[] placeHolders) {
        this.placeHolders = placeHolders;
    }

    public EqlParamPlaceholder[] getPlaceHolders() {
        return placeHolders;
    }
}
