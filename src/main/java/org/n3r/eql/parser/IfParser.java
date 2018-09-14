package org.n3r.eql.parser;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.val;
import org.n3r.eql.util.S;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.n3r.eql.parser.TrimParser.cleanLine;

public class IfParser implements PartParser {
    private String lastCondExpr;
    private MultiPart multiPart = new MultiPart();
    private List<IfCondition> conditions = Lists.newArrayList();

    public IfParser(String firstCondExpr) {
        this.lastCondExpr = firstCondExpr;
    }

    @Override
    public EqlPart createPart() {
        return new IfPart(conditions);
    }

    static Pattern elseIfPattern = Pattern.compile("else\\s?if\\b(.*)", Pattern.CASE_INSENSITIVE);

    @Override
    public int parse(List<String> mergedLines, int index) {
        boolean elseReached = false;

        int i = index;
        for (int ii = mergedLines.size(); i < ii; ++i) {
            String line = mergedLines.get(i);

            val clearLineRet = cleanLine(line, multiPart);
            if (clearLineRet._2 != null) continue;

            val clearLine = clearLineRet._1;

            if ("end".equalsIgnoreCase(clearLine)) {
                newCondition();
                return i + 1;
            }

            if ("else".equalsIgnoreCase(clearLine)) {
                newCondition();
                lastCondExpr = "true";
                elseReached = true;
                continue;
            }

            Matcher matcher = elseIfPattern.matcher(clearLine);
            if (matcher.matches()) { // else if
                if (elseReached)
                    throw new RuntimeException("syntax error, else if position is illegal");

                newCondition();
                lastCondExpr = S.trimToEmpty(matcher.group(1));
                if (S.isBlank(lastCondExpr))
                    throw new RuntimeException("syntax error, no condition in else if");

                continue;
            }

            PartParser partParser = PartParserFactory.tryParse(clearLine);
            if (partParser != null) {
                i = partParser.parse(mergedLines, i + 1) - 1;
                multiPart.addPart(partParser.createPart());
            }
        }

        return i;
    }

    private void newCondition() {
        if (Strings.isNullOrEmpty(lastCondExpr) || multiPart.size() == 0) return;

        conditions.add(new IfCondition(lastCondExpr, multiPart));
        lastCondExpr = null;
        multiPart = new MultiPart();
    }

}
