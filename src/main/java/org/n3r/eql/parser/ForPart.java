package org.n3r.eql.parser;

import ognl.Ognl;
import ognl.OgnlException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForPart implements EqlPart {
    private LiteralPart part;
    private String item;
    private String index;
    private String collection;
    private String open;
    private String separator;
    private String close;

    public ForPart(LiteralPart part, String item, String index, String collection, String open, String separator, String close) {
        this.part = part;
        this.item = item;
        this.index = index;
        this.collection = collection;
        this.open = open;
        this.separator = separator;
        this.close = close;
    }


    public LiteralPart getSqlPart() {
        return part;
    }

    private static Pattern PARAM_PATTERN = Pattern.compile("#\\s*(.+?)\\s*#");

    @Override
    public String evalSql(Object bean, Map<String, Object> executionContext) {
        StringBuilder str = new StringBuilder(open);
        String sql = part.getSql();

        Map<Object, Object> context = new HashMap<Object, Object>(executionContext);

        Collection<?> items = evalCollection(bean);
        int i = -1;
        for (Object itemObj : items) {
            if (++i > 0) str.append(separator);

            context.put(index, i);
            context.put(item, itemObj);

            Matcher matcher = PARAM_PATTERN.matcher(sql);
            int startIndex = 0;

            while (matcher.find()) {
                str.append(sql.substring(startIndex, matcher.start()));
                startIndex = matcher.end();
                String expr = matcher.group(1);
                if (item.equals(expr)) str.append("#" + collection + "[" + i + "]#");
                else if (index.equals(index)) str.append(i);
                else str.append(expr);
            }

            if (startIndex < sql.length()) str.append(sql.substring(startIndex));
        }

        str.append(close);
        return str.toString();
    }

    public Object eval(Object bean, String expr, Map<Object, Object> context) {
        try {
            return Ognl.getValue(expr, context, bean);
        } catch (OgnlException e) {
            throw new RuntimeException("eval " + expr + " within " + bean + " and context " + context + " failed", e);
        }
    }

    private Collection<?> evalCollection(Object bean) {
        try {
            Object value = Ognl.getValue(collection, bean);
            if (value instanceof Collection) {
                return (Collection<?>) value;
            }
            throw new RuntimeException(collection + " in " + bean + " is not a collection");
        } catch (OgnlException e) {
            throw new RuntimeException("eval expr " + collection + " error", e);
        }
    }
}
