package org.n3r.eql.parser;

import java.util.List;
import java.util.regex.Matcher;

public class IffParser implements PartParser {
    private final String expr;
    private LiteralPart part = new LiteralPart("");

    public IffParser(String expr) {
        this.expr = expr;
    }

    @Override
    public EqlPart createPart() {
        return new IffPart(expr, part);
    }

    @Override
    public int parse(List<String> mergedLines, int index) {
        int i = index;
        for (int ii = mergedLines.size(); i < ii; ++i) {
            String line = mergedLines.get(i);
            String clearLine = parseClearLine(line);

            PartParser partParser = clearLine == null
                    ? null : PartParserFactory.tryParse(clearLine);
            if (partParser != null) return i;

            if (clearLine != null) part.appendComment(line);
            else part.appendSql(line);
        }

        return i;
    }

    private String parseClearLine(String line) {
        if (line.startsWith("--")) return ParserUtils.substr(line, "--".length());

        Matcher matcher = ParserUtils.inlineComment.matcher(line);
        if (matcher.matches()) return matcher.group(1).trim();

        return null;
    }
}
