package org.n3r.eql.param;


import lombok.Data;
import org.n3r.eql.map.EqlType;

@Data
public class EqlParamsParserResult {
    private EqlType sqlType;
    private String runSql;
    private int placeholderNum;
    private PlaceholderType placeHolderType;
    private PlaceholderType placeHolderOutType;
    private EqlParamPlaceholder[] placeHolders;
    private String evalSqlTemplate;

    public void setEvalSqlTemplate(String evalSqlTemplate) {
        this.evalSqlTemplate = evalSqlTemplate.replaceAll("\\r?\\n", " ");
    }
}
