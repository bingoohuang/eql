package org.n3r.eql.parser;

import lombok.val;

import java.util.List;

public class IsEmptyParser implements PartParser {
    protected final String expr;
    protected MultiPart multiPart = new MultiPart();
    protected MultiPart elsePart = new MultiPart();

    public IsEmptyParser(String expr) {
        this.expr = expr;
    }

    @Override
    public EqlPart createPart() {
        return new IsEmptyPart(expr, multiPart, elsePart);
    }

    @Override
    public int parse(List<String> mergedLines, int index) {
        int i = index;
        EqlPart lastPart = null;
        MultiPart current = multiPart;
        for (int ii = mergedLines.size(); i < ii; ++i) {
            String line = mergedLines.get(i);

            val clearLineRet = TrimParser.cleanLine(line, current);
            if (clearLineRet._2 != null) {
                lastPart = clearLineRet._2;
                continue;
            }

            val clearLine = clearLineRet._1;

            if ("end".equalsIgnoreCase(clearLine)) {
                return i + 1;
            }

            if ("else".equalsIgnoreCase(clearLine)) {
                current = elsePart;
                continue;
            }

            PartParser partParser = PartParserFactory.tryParse(clearLine);
            if (partParser != null) {
                i = partParser.parse(mergedLines, i + 1) - 1;

                current.addPart(partParser.createPart());
                lastPart = null;
            } else if (lastPart instanceof LiteralPart) {
                ((LiteralPart) lastPart).appendComment(line);
            }
        }

        return i;
    }
}
