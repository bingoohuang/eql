package org.n3r.eql.parser;


import lombok.val;
import org.n3r.eql.util.PairsParser;

import java.util.List;
import java.util.Map;

public class ForParser implements PartParser {
    private String item = "item";
    private String index = "index";
    private String collection;
    private String open = "";
    private String separator = "";
    private String close = "";
    private MultiPart part = new MultiPart();

    // for item=item index=index collection=list open=( separator=, close=)
    public ForParser(String options) {
        Map<String, String> optionsMap = new PairsParser().parse(options.trim());
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

            val clearLineRet = TrimParser.cleanLine(line, part);
            if (clearLineRet._2 != null) continue;

            val clearLine = clearLineRet._1;

            if ("end".equalsIgnoreCase(clearLine)) return i + 1;

            PartParser partParser = PartParserFactory.tryParse(clearLine);
            if (partParser != null) {
                i = partParser.parse(mergedLines, i + 1) - 1;
                part.addPart(partParser.createPart());
            }
        }

        return i;
    }
}
