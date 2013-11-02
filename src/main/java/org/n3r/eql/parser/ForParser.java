package org.n3r.eql.parser;

import com.google.common.base.Splitter;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class ForParser implements PartParser {
    private String item = "item";
    private String index = "index";
    private String collection;
    private String open = "";
    private String separator = "";
    private String close = "";
    private LiteralPart part = new LiteralPart("");

    // for item=item index=index collection=list open=( separator=, close=)
    public ForParser(String options) {
        Map<String, String> optionsMap = Splitter.on(' ').trimResults()
                .withKeyValueSeparator('=').split(options.trim());
        if (optionsMap.containsKey("item")) item = optionsMap.get("item");
        if (optionsMap.containsKey("index")) index = optionsMap.get("index");
        if (optionsMap.containsKey("collection")) collection = optionsMap.get("collection");
        if (optionsMap.containsKey("open")) open = optionsMap.get("open");
        if (optionsMap.containsKey("separator")) separator = optionsMap.get("separator");
        if (optionsMap.containsKey("close")) close = optionsMap.get("close");

        if (ParserUtils.isBlank(collection))
            throw new RuntimeException("for clause required collection");
    }

    @Override
    public EqlPart createPart() {
        return new ForPart(part, item, index, collection, open, separator, close);
    }

    @Override
    public int parse(List<String> mergedLines, int index) {
        int i = index;
        for (int ii = mergedLines.size(); i < ii; ++i) {
            String line = mergedLines.get(i);

            String clearLine = parseClearLine(line);

            if ("end".equalsIgnoreCase(clearLine)) {
                return i + 1;
            }

            if (clearLine != null) part.appendComment(line);
            else part.appendSql(line);
        }

        return i;
    }

    private String parseClearLine(String line) {
        if (line.startsWith("--")) {
            return ParserUtils.substr(line, "--".length());
        }

        Matcher matcher = ParserUtils.inlineComment.matcher(line);
        if (matcher.matches()) return matcher.group(1).trim();

        return null;
    }
}
