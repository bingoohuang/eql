package org.n3r.eql.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockOptionsParser {
    private static Pattern OPTION_PATTERN = Pattern.compile("\\b(\\w+)\\b\\s*(=?)\\s*");

    public static Map<String, String> parseOptions(String optionsStr, OptionValueParser... optionValueParsers) {
        HashMap<String, String> options = new HashMap<String, String>();
        Matcher matcher = OPTION_PATTERN.matcher(optionsStr);
        int pos = 0;
        while (matcher.find(pos)) {
            String key = matcher.group(1);
            String option = "";
            pos = matcher.end();
            boolean haveOptionValue = "=".equals(matcher.group(2));

            if (haveOptionValue) {
                OptionValueParser valueParser = null;
                for (OptionValueParser optionValueParser : optionValueParsers) {
                    if (optionValueParser.getKey().equals(key)) {
                        valueParser = optionValueParser;
                        break;
                    }
                }
                if (valueParser == null) valueParser = WordOptionValueParser.instance;

                OffsetAndOptionValue oo = valueParser.parseValueOption(optionsStr.substring(pos));
                option = oo.getOptionValue();
                pos += oo.getOffset();
            }

            options.put(key, option);

        }
        return options;
    }

}
