package org.n3r.eql.parser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.EqlUtils;
import org.n3r.eql.util.S;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
public class ForPart implements EqlPart {
    @Getter
    private MultiPart part;
    private String item;
    private String index;
    private String collection;
    private String open;
    private String separator;
    private String close;

    private static Pattern PARAM_PATTERN = Pattern.compile("#\\s*(.+?)\\s*#");
    private static Pattern DYNAMIC_PATTERN = Pattern.compile("\\$\\s*(.+?)\\s*\\$");

    @Override
    public String evalSql(EqlRun eqlRun) {
        val items = EqlUtils.evalCollection(collection, eqlRun);
        if (items == null) return "";

        val collectionExpr = EqlUtils.collectionExprString(collection, eqlRun);
        val preContext = eqlRun.getExecutionContext();
        val context = new HashMap<String, Object>(preContext);
        eqlRun.setExecutionContext(context);

        val str = new StringBuilder(open).append(' ');

        val itemPattern = Pattern.compile("\\b(?<!.)" + item + "\\b");
        val indexPattern = Pattern.compile("\\b(?<!.)" + index + "\\b");

        int i = -1;
        for (Object itemObj : items) {
            context.put(index, ++i);
            context.put(item, itemObj);

            String sql = part.evalSql(eqlRun);
            sql = processParams(PARAM_PATTERN, '#', itemPattern, indexPattern, collectionExpr, i, sql);
            sql = processParams(DYNAMIC_PATTERN, '$', itemPattern, indexPattern, collectionExpr, i, sql);

            if (i > 0 && S.isNotBlank(sql)) str.append(separator);

            str.append(sql);
        }

        str.append(close);

        eqlRun.setExecutionContext(preContext);
        return str.toString();
    }

    private String processParams(Pattern pattern, char ch,
                                 Pattern itemPattern, Pattern indexPattern,
                                 String collectionExpr, int idx, String sql) {
        int startIndex = 0;
        StringBuilder str = new StringBuilder();
        String colItem = collectionExpr + "[" + idx + "]";

        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            str.append(sql, startIndex, matcher.start());
            startIndex = matcher.end();
            String expr = matcher.group(1);

            if (item.equals(expr)) {
                str.append(S.wrap(colItem, ch));
            } else if (index.equals(expr)) {
                str.append(idx);
            } else {
                val itemMatcher = itemPattern.matcher(expr);
                String s = itemMatcher.replaceAll(colItem);
                val indexMatcher = indexPattern.matcher(s);
                s = indexMatcher.replaceAll("" + idx);

                str.append(S.wrap(S.escapeCrossAndDollar(s), ch));
            }
        }

        if (startIndex < sql.length()) {
            str.append(sql.substring(startIndex));
        }

        return str.toString();
    }


}
