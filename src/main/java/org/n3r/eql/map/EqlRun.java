package org.n3r.eql.map;

import com.google.common.base.Throwables;
import org.n3r.eql.param.EqlParamPlaceholder;
import org.n3r.eql.param.PlaceholderType;
import org.n3r.eql.parser.SqlBlock;

public class EqlRun implements Cloneable {
    public static enum EqlType {
        SELECT, UPDATE, INSERT, MERGE, DELETE,
        DROP, CREATE, TRUNCATE, CALL, COMMENT, ALTER, BEGIN, DECLARE;
    }

    private String runSql;
    private String printSql;

    private SqlBlock sqlBlock;
    private String sql;
    private int placeholderNum;
    private EqlParamPlaceholder[] placeHolders;
    private PlaceholderType placeHolderType;
    private PlaceholderType placeHolderOutType;
    private EqlType sqlType;
    private boolean lastSelectSql;
    private boolean willReturnOnlyOneRow;
    private Object[] extraBindParams;
    private EqlDynamic eqlDynamic;
    private int outCount;

    @Override
    public EqlRun clone() {
        try {
            return (EqlRun) super.clone();
        } catch (CloneNotSupportedException e) {
            throw Throwables.propagate(e);
        }
    }

    public String getPrintSql() {
        return printSql;
    }

    public void setPrintSql(String printSql) {
        this.printSql = printSql;
    }

    public void setRunSql(String runSql) {
        this.runSql = runSql;
    }

    public String getRunSql() {
        return runSql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getSqlId() {
        return sqlBlock != null ? sqlBlock.getSqlId() : "<AUTO>";
    }

    public void setPlaceHolders(EqlParamPlaceholder[] placeHolders) {
        this.placeHolders = placeHolders;
        outCount = 0;
        for (EqlParamPlaceholder placeHolder : placeHolders)
            if (placeHolder.getInOut() != EqlParamPlaceholder.InOut.IN) ++outCount;
    }

    public String getSql() {
        return sql;
    }

    public EqlParamPlaceholder[] getPlaceHolders() {
        return placeHolders;
    }

    public EqlParamPlaceholder getPlaceHolder(int index) {
        return index < placeHolders.length ? placeHolders[index] : null;
    }

    public void setPlaceHolderType(PlaceholderType placeHolderType) {
        this.placeHolderType = placeHolderType;
    }

    public PlaceholderType getPlaceHolderType() {
        return placeHolderType;
    }

    public int getPlaceholderNum() {
        return placeholderNum;
    }

    public void setPlaceholderNum(int placeholderNum) {
        this.placeholderNum = placeholderNum;
    }

    public SqlBlock getSqlBlock() {
        return sqlBlock;
    }

    public void setSqlBlock(SqlBlock sqlBlock) {
        this.sqlBlock = sqlBlock;
    }

    public EqlType getSqlType() {
        return sqlType;
    }

    public void setSqlType(EqlType sqlType) {
        this.sqlType = sqlType;
    }

    public void setLastSelectSql(boolean lastSelectSql) {
        this.lastSelectSql = lastSelectSql;
    }

    public boolean isLastSelectSql() {
        return lastSelectSql;
    }

    public boolean isWillReturnOnlyOneRow() {
        return willReturnOnlyOneRow;
    }

    public void setWillReturnOnlyOneRow(boolean willReturnOnlyOneRow) {
        this.willReturnOnlyOneRow = willReturnOnlyOneRow;
    }

    public Object[] getExtraBindParams() {
        return extraBindParams;
    }

    public void setExtraBindParams(Object... extraBindParams) {
        this.extraBindParams = extraBindParams;
    }

    public void setEqlDynamic(EqlDynamic eqlDynamic) {
        this.eqlDynamic = eqlDynamic;
    }

    public EqlDynamic getEqlDynamic() {
        return eqlDynamic;
    }

    public int getOutCount() {
        return outCount;
    }

    public PlaceholderType getPlaceHolderOutType() {
        return placeHolderOutType;
    }

    public void setPlaceHolderOutType(PlaceholderType placeHolderOutType) {
        this.placeHolderOutType = placeHolderOutType;
    }

}
