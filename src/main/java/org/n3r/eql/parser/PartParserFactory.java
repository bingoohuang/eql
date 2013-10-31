package org.n3r.eql.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartParserFactory {
    static Pattern pattern = Pattern.compile("(\\w+)\\b(.*)");

    public static PartParser tryParse(String clearLine) {
        Matcher matcher = pattern.matcher(clearLine);
        if (!matcher.matches()) return null;

        String keyword = matcher.group(1).toLowerCase();
        String option = matcher.group(2);
        if (keyword.equals("if")) return new IfParser(option);
        if (keyword.equals("isempty")) return new IsEmptyParser(option);
        if (keyword.equals("isnotempty")) return new IsNotEmptyParser(option);
        if (keyword.equals("isnull")) return new IsNullParser(option);
        if (keyword.equals("isnotnull")) return new IsNotNullParser(option);
        if (keyword.equals("isblank")) return new IsBlankParser(option);
        if (keyword.equals("isnotblank")) return new IsNotBlankParser(option);
        if (keyword.equals("switch")) return new SwitchParser(option);
        if (keyword.equals("for")) return new ForParser(option);

        return null;
    }

}
