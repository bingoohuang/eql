package org.n3r.eql.parser;

import java.util.List;
import java.util.regex.Matcher;

public class IsEmptyParser implements PartParser {
    protected final String expr;
    protected MultiPart multiPart = new MultiPart();

    public IsEmptyParser(String expr) {
        this.expr = expr;
    }

    @Override
    public EqlPart createPart() {
        return new IsEmptyPart(expr, multiPart);
    }

    @Override
    public int parse(List<String> mergedLines, int index) {
        int i = index;
        EqlPart lastPart = null;
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
                    multiPart.addPart(lastPart);
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
                lastPart = null;
            } else if (lastPart instanceof LiteralPart) {
                ((LiteralPart) lastPart).appendComment(line);
            }
        }

        return i;
    }
}
