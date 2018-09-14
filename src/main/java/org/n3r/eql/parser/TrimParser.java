package org.n3r.eql.parser;

import lombok.val;
import org.n3r.eql.util.Pair;
import org.n3r.eql.util.PairsParser;

import java.util.List;
import java.util.Map;

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
        Map<String, String> optionsMap = new PairsParser().parse(options.trim());
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

            val clearLineRet = cleanLine(line, multiPart);
            if (clearLineRet._2 != null) continue;

            val clearLine = clearLineRet._1;

            if ("end".equalsIgnoreCase(clearLine)) {
                return i + 1;
            }

            val partParser = PartParserFactory.tryParse(clearLine);
            if (partParser != null) {
                i = partParser.parse(mergedLines, i + 1) - 1;

                multiPart.addPart(partParser.createPart());
            }
        }

        return i;
    }

    public static Pair<String, LiteralPart> cleanLine(String line, MultiPart multiPart) {
        if (line.startsWith("--")) {
            return Pair.of(ParserUtils.substr(line, "--".length()), null);
        }

        val matcher = ParserUtils.inlineComment.matcher(line);
        if (matcher.matches()) {
            return Pair.of(matcher.group(1).trim(), null);
        }

        LiteralPart part = new LiteralPart(line);
        multiPart.addPart(part);
        return Pair.of(null, part);
    }
}
