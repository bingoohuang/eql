package org.n3r.eql.map;

import com.google.common.base.Throwables;
import org.n3r.eql.param.EqlParamPlaceholder;
import org.n3r.eql.param.PlaceholderType;
import org.n3r.eql.parser.EqlBlock;

public class EqlRun implements Cloneable {
    public static enum EqlType {
        SELECT, UPDATE, INSERT, MERGE, DELETE,
        DROP, CREATE, TRUNCATE, CALL, COMMENT, ALTER, BEGIN, DECLARE;
    }

    private String runSql;
    private String printSql;
    private Object result;

    private EqlBlock eqlBlock;
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

    public void setResult(Object result) {
        this.result = result;
    }

    public Object getResult() {
        return result;
    }

    public String getPrintSql() {
        return printSql;
    }

    public void createPrintSql() {
        printSql = runSql.replaceAll("\\r?\\n", "\\\\n");
    }


    public void setRunSql(String runSql) {
        this.runSql = runSql;
    }

    public String getRunSql() {
        return runSql;
    }

    public String getSqlId() {
        return eqlBlock != null ? eqlBlock.getSqlId() : "<AUTO>";
    }

    public void setPlaceHolders(EqlParamPlaceholder[] placeHolders) {
        this.placeHolders = placeHolders;
        outCount = 0;
        for (EqlParamPlaceholder placeHolder : placeHolders)
            if (placeHolder.getInOut() != EqlParamPlaceholder.InOut.IN) ++outCount;
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

    public EqlBlock getEqlBlock() {
        return eqlBlock;
    }

    public void setEqlBlock(EqlBlock eqlBlock) {
        this.eqlBlock = eqlBlock;
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
