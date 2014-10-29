package org.n3r.eql.parser;

import org.n3r.eql.base.ExpressionEvaluator;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.EqlUtils;
import org.n3r.eql.util.S;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForPart implements EqlPart {
    private MultiPart part;
    private String item;
    private String index;
    private String collection;
    private String open;
    private String separator;
    private String close;

    public ForPart(MultiPart part, String item, String index, String collection, String open, String separator, String close) {
        this.part = part;
        this.item = item;
        this.index = index;
        this.collection = collection;
        this.open = open;
        this.separator = separator;
        this.close = close;
    }

    public MultiPart getSqlPart() {
        return part;
    }

    private static Pattern PARAM_PATTERN = Pattern.compile("#\\s*(.+?)\\s*#");
    private static Pattern DYNAMIC_PATTERN = Pattern.compile("\\$\\s*(.+?)\\s*\\$");

    @Override
    public String evalSql(EqlRun eqlRun) {
        StringBuilder str = new StringBuilder(open).append(' ');

        Collection<?> items = EqlUtils.evalCollection(collection, eqlRun);
        if (items == null || items.size() == 0) return "";

        Map<String, Object> preContext = eqlRun.getExecutionContext();
        Map<String, Object> context = new HashMap<String, Object>(preContext);
        eqlRun.setExecutionContext(context);

        Pattern itemPattern = Pattern.compile("\\b" + item + "\\b");
        Pattern indexPattern = Pattern.compile("\\b" + index + "\\b");

        int i = -1;
        for (Object itemObj : items) {
            context.put(index, ++i);
            context.put(item, itemObj);

            String sql = part.evalSql(eqlRun);
            sql = processParams(PARAM_PATTERN, '#', itemPattern, indexPattern, i, sql);
            sql = processParams(DYNAMIC_PATTERN, '$', itemPattern, indexPattern, i, sql);

            if (i > 0 && S.isNotBlank(sql)) str.append(separator);

            str.append(sql);
        }

        str.append(close);

        eqlRun.setExecutionContext(preContext);
        return str.toString();
    }

    private String processParams(Pattern pattern, char ch,
                                 Pattern itemPattern, Pattern indexPattern,
                                 int idx, String sql) {
        Matcher matcher = pattern.matcher(sql);
        int startIndex = 0;
        StringBuilder str = new StringBuilder();

        while (matcher.find()) {
            str.append(sql.substring(startIndex, matcher.start()));
            startIndex = matcher.end();
            String expr = matcher.group(1);

            if (item.equals(expr)) str.append(S.wrap(collection + "[" + idx + "]", ch));
            else if (this.index.equals(expr)) str.append(idx);
            else {
                String s = itemPattern.matcher(expr).replaceAll(collection + "[" + idx + "]");
                s = indexPattern.matcher(s).replaceAll("" + idx);

                str.append(S.wrap(S.escapeCrossAndDollar(s), ch));
            }
        }

        if (startIndex < sql.length()) str.append(sql.substring(startIndex));

        return str.toString();
    }



}
