package org.n3r.eql.parser;

import java.util.List;
import java.util.regex.Matcher;

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

            String clearLine;
            if (line.startsWith("--")) {
                clearLine = ParserUtils.substr(line, "--".length());
            } else {
                Matcher matcher = ParserUtils.inlineComment.matcher(line);
                if (matcher.matches()) {
                    clearLine = matcher.group(1).trim();
                } else {
                    lastPart = new LiteralPart(line);
                    current.addPart(lastPart);
                    continue;
                }
            }

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
