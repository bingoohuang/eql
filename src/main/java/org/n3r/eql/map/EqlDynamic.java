package org.n3r.eql.map;

import java.util.List;

import org.n3r.eql.param.EqlParamPlaceholder;
import org.n3r.eql.param.PlaceholderType;

public class EqlDynamic {
    private List<String> sqlPieces;
    private PlaceholderType placeholdertype;
    private EqlParamPlaceholder[] placeholders;

    public void setSqlPieces(List<String> sqlPieces) {
        this.sqlPieces = sqlPieces;
    }

    public List<String> getSqlPieces() {
        return sqlPieces;
    }

    public PlaceholderType getPlaceholdertype() {
        return placeholdertype;
    }

    public void setPlaceholdertype(PlaceholderType placeholdertype) {
        this.placeholdertype = placeholdertype;
    }

    public EqlParamPlaceholder[] getPlaceholders() {
        return placeholders;
    }

    public void setPlaceholders(EqlParamPlaceholder[] placeholders) {
        this.placeholders = placeholders;
    }

}
