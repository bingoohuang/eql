package org.n3r.eql.parser;

import org.n3r.eql.util.S;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartParserFactory {
    static Pattern pattern = Pattern.compile("(\\w+)\\b(.*)", Pattern.DOTALL);

    public static PartParser tryParse(String clearLine) {
        Matcher matcher = pattern.matcher(clearLine);
        if (!matcher.matches()) return null;

        String keyword = matcher.group(1).toLowerCase();
        String option = S.trimToEmpty(matcher.group(2));

        PartParser partParser = null;
        if (keyword.equals("if")) partParser = new IfParser(option);
        else if (keyword.equals("iff")) partParser = new IffParser(option);
        else if (keyword.equals("unless")) partParser = new UnlessParser(option);
        else if (keyword.equals("isempty")) partParser = new IsEmptyParser(option);
        else if (keyword.equals("isnotempty")) partParser = new IsNotEmptyParser(option);
        else if (keyword.equals("isnull")) partParser = new IsNullParser(option);
        else if (keyword.equals("isnotnull")) partParser = new IsNotNullParser(option);
        else if (keyword.equals("isblank")) partParser = new IsBlankParser(option);
        else if (keyword.equals("isnotblank")) partParser = new IsNotBlankParser(option);
        else if (keyword.equals("switch")) partParser = new SwitchParser(option);
        else if (keyword.equals("for")) partParser = new ForParser(option);
        else if (keyword.equals("trim")) partParser = new TrimParser(option);

        if (partParser != null && S.isBlank(option))
            throw new RuntimeException(clearLine + " is invalid");

        return partParser;
    }

}
