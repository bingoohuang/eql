package org.n3r.eql.parser;

import lombok.val;

import java.util.List;

public class UnlessParser implements PartParser {
    private String expr;
    private MultiPart multiPart = new MultiPart();

    public UnlessParser(String expr) {
        this.expr = expr;
    }

    @Override
    public EqlPart createPart() {
        return new UnlessPart(expr, multiPart);
    }

    @Override
    public int parse(List<String> mergedLines, int index) {
        int i = index;
        for (int ii = mergedLines.size(); i < ii; ++i) {
            String line = mergedLines.get(i);

            val clearLineRet = TrimParser.cleanLine(line, multiPart);
            if (clearLineRet._2 != null) continue;

            val clearLine = clearLineRet._1;

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
