package org.n3r.eql.parser;

import com.google.common.collect.Lists;
import lombok.val;
import org.n3r.eql.ex.EqlConfigException;
import org.n3r.eql.map.EqlDynamic;
import org.n3r.eql.param.EqlParamPlaceholder;
import org.n3r.eql.param.PlaceholderType;
import org.n3r.eql.util.S;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DynamicParser {
    private static final Pattern DYNAMIC_PATTERN = Pattern.compile("'?\\$(.*?)\\$'?");
    private EqlDynamic dynamicSql;
    private String rawSql;

    public EqlDynamic parseRawSql(String rawSql) {
        this.rawSql = rawSql;
        this.dynamicSql = new EqlDynamic();

        Matcher matcher = DYNAMIC_PATTERN.matcher(rawSql);
        List<String> placeHolders = Lists.newArrayList();
        List<String> sqlPieces = Lists.newArrayList();
        int startPos = 0;
        while (matcher.find()) {
            String placeHolder = matcher.group(1).trim();
            placeHolders.add(placeHolder);

            sqlPieces.add(rawSql.substring(startPos, matcher.start()));
            startPos = matcher.end();
        }

        if (startPos == 0) return null;

        sqlPieces.add(rawSql.substring(startPos));

        dynamicSql.setSqlPieces(sqlPieces);

        parsePlaceholders(placeHolders);

        return dynamicSql;
    }

    private void parsePlaceholders(List<String> placeHolders) {
        List<EqlParamPlaceholder> paramPlaceholders = new ArrayList<>();

        PlaceholderType placeHoldertype = PlaceholderType.UNSET;
        for (String placeHolder : placeHolders) {
            val paramPlaceholder = new EqlParamPlaceholder();
            paramPlaceholders.add(paramPlaceholder);
            paramPlaceholder.setPlaceholder(placeHolder);

            if (placeHolder.length() == 0) {
                paramPlaceholder.setPlaceholderType(PlaceholderType.AUTO_SEQ);
            } else if (S.isInteger(placeHolder)) {
                paramPlaceholder.setPlaceholderType(PlaceholderType.MANU_SEQ);
                paramPlaceholder.setSeq(Integer.valueOf(placeHolder));
            } else {
                paramPlaceholder.setPlaceholderType(PlaceholderType.VAR_NAME);
            }

            placeHoldertype = paramPlaceholder.getPlaceholderType();
        }

        for (val pPlaceholder : paramPlaceholders) {
            if (placeHoldertype == pPlaceholder.getPlaceholderType()) continue;

            throw new EqlConfigException("[" + rawSql + "]中定义的SQL动态替换设置类型不一致");
        }

        dynamicSql.setPlaceholdertype(placeHoldertype);
        val placeholders = paramPlaceholders.toArray(new EqlParamPlaceholder[0]);
        dynamicSql.setPlaceholders(placeholders);
    }
}
