package org.n3r.eql.parser;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SwitchParser implements PartParser {
    private final String condition;
    private List<IfCondition> cases = Lists.<IfCondition>newArrayList();

    private String lastCondExpr;
    private MultiPart multiPart = new MultiPart();
    private EqlPart lastPart;

    public SwitchParser(String condition) {
        this.condition = condition;
    }

    @Override
    public EqlPart createPart() {
        return new SwitchPart(condition, cases);
    }

    static Pattern casePattern = Pattern.compile("case\\s+(.*)", Pattern.CASE_INSENSITIVE);

    @Override
    public int parse(List<String> mergedLines, int index) {
        boolean defaultReached = false;

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
                    lastPart = new LiteralPart(line);
                    multiPart.addPart(lastPart);
                    continue;
                }
            }

            if ("end".equalsIgnoreCase(clearLine)) {
                newCondition();
                return i + 1;
            }

            if ("default".equalsIgnoreCase(clearLine)) {
                newCondition();
                lastCondExpr = "";
                defaultReached = true;
                continue;
            }

            Matcher matcher = casePattern.matcher(clearLine);
            if (matcher.matches()) { // case xxx
                if (defaultReached)
                    throw new RuntimeException("syntax error, case position is illegal");

                newCondition();
                lastCondExpr = matcher.group(1);
                continue;
            }

            PartParser partParser = PartParserFactory.tryParse(clearLine);
            if (partParser != null) {
                i = partParser.parse(mergedLines, i + 1) - 1;

                lastPart = partParser.createPart();
                multiPart.addPart(lastPart);
                newCondition();
            } else if (lastPart instanceof LiteralPart) {
                ((LiteralPart) lastPart).appendComment(line);
            }
        }

        return i;
    }

    private void newCondition() {
        if (lastCondExpr == null || multiPart.size() == 0) return;

        cases.add(new IfCondition(lastCondExpr, multiPart));
        lastCondExpr = null;
        lastPart = null;
        multiPart = new MultiPart();
    }
}
