package org.n3r.eql.parser;

import org.n3r.eql.util.EqlUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockOptionsParser {
    private static Pattern OPTION_PATTERN = Pattern.compile("\\b(\\w+)\\b\\s*(=\\s*(\"?[^\"]+\"?))?");

    public static Map<String, String> parseOptions(String optionsStr) {
        HashMap<String, String> options = new HashMap<String, String>();
        Matcher matcher = OPTION_PATTERN.matcher(optionsStr);
        int pos = 0;
        while (matcher.find(pos)) {
            String key = matcher.group(1);
            String option = matcher.group(3);
            pos = matcher.end();
            if (option != null &&
                    (option.startsWith("\"") && !option.endsWith("\"") || !option.startsWith("\""))) {
                int blankPos = EqlUtils.indexOfBlank(option);
                if (blankPos >= 0) {
                    pos = matcher.start(3) + blankPos;
                    option = option.substring(0, blankPos);
                }
            }

            options.put(key, EqlUtils.cleanQuote(option));

        }
        return options;
    }

}
