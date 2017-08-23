package org.n3r.eql.map;

import lombok.Data;
import org.n3r.eql.param.EqlParamPlaceholder;
import org.n3r.eql.param.PlaceholderType;

import java.util.List;

@Data
public class EqlDynamic {
    private List<String> sqlPieces;
    private PlaceholderType placeholdertype;
    private EqlParamPlaceholder[] placeholders;
}
