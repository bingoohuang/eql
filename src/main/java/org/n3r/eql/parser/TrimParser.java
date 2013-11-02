package org.n3r.eql.parser;

import com.google.common.base.Splitter;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class TrimParser implements PartParser {
    private final String prefix;
    private final String suffix;
    private final String prefixOverrides;
    private final String suffixOverrides;

    private MultiPart multiPart = new MultiPart();


    // trim prefix=WHERE prefixOverrides=AND|OR
    // trim prefix=SET suffixOverrides=,
    // trim prefix=( prefixOverrides=OR suffix=)
    public TrimParser(String options) {
        Map<String, String> optionsMap = Splitter.on(' ').trimResults()
                .withKeyValueSeparator('=').split(options.trim());
        prefix = optionsMap.get("prefix");
        suffix = optionsMap.get("suffix");
        prefixOverrides = optionsMap.get("prefixOverrides");
        suffixOverrides = optionsMap.get("suffixOverrides");
    }

    @Override
    public EqlPart createPart() {
        return new TrimPart(prefix, suffix, prefixOverrides, suffixOverrides, multiPart);
    }

    @Override
    public int parse(List<String> mergedLines, int index) {
        int i = index;
        for (int ii = mergedLines.size(); i < ii; ++i) {
            String line = mergedLines.get(i);

            String clearLine;
            if (line.startsWith("--")) {
                clearLine = ParserUtils.substr(line, "--".length());
            } else {
                Matcher matcher = ParserUtils.inlineComment.matcher(line);
                if (matcher.matches()) {
                    clearLine = matcher.group(1).trim();
                } else {
                    multiPart.addPart(new LiteralPart(line));
                    continue;
                }
            }

            if ("end".equalsIgnoreCase(clearLine)) {
                return i + 1;
            }

            PartParser partParser = PartParserFactory.tryParse(clearLine);
            if (partParser != null) {
                i = partParser.parse(mergedLines, i + 1) - 1;

                multiPart.addPart(partParser.createPart());
            }
        }

        return i;
    }
}
