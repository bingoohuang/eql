package org.n3r.eql.parser;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.regex.Matcher;

public class EqlBlockParser {
    private List<Eql> eqls = Lists.newArrayList();

    public void parse(EqlBlock block, List<String> sqlLines) {
        List<String> oneSqlLines = Lists.newArrayList();

        // split to multiple sql
        for (String sqlLine : sqlLines) {
            if (sqlLine.endsWith(";")) {
                oneSqlLines.add(sqlLine.substring(0, sqlLine.length() - 1));
                addSql(oneSqlLines);
            } else {
                oneSqlLines.add(sqlLine);
            }
        }

        addSql(oneSqlLines);

        block.setEqls(eqls);
        block.setSqlLines(sqlLines);
    }

    private void addSql(List<String> oneSqlLines) {
        if (oneSqlLines.size() == 0) return;

        Eql eql = parseSql(oneSqlLines);
        if (eql != null) eqls.add(eql);
        oneSqlLines.clear();
    }


    private Eql parseSql(List<String> oneSqlLines) {
        List<String> stdLines = standardLines(oneSqlLines);

        MultiPart multiPart = new MultiPart();

        for (int i = 0, ii = stdLines.size(); i < ii; ++i) {
            String line = stdLines.get(i);

            if (line.startsWith("--")) {
                String clearLine = ParserUtils.substr(line, "--".length());
                PartParser partParser = PartParserFactory.tryParse(clearLine);
                if (partParser != null) {
                    i = partParser.parse(stdLines, i + 1) - 1;
                    multiPart.addPart(partParser.createPart());
                }
                continue;
            }

            Matcher matcher = ParserUtils.inlineComment.matcher(line);
            if (!matcher.matches()) {
                multiPart.addPart(new LiteralPart(line));
                continue;
            }

            String cleanStr = matcher.group(1);
            PartParser partParser = PartParserFactory.tryParse(cleanStr);
            if (partParser != null) {
                i = partParser.parse(stdLines, i + 1) - 1;
                multiPart.addPart(partParser.createPart());
            } else {
                multiPart.addPart(new LiteralPart(line));
            }
        }

        if (multiPart.size() == 0) return null;

        if (multiPart.size() == 1 && multiPart.part(0) instanceof LiteralPart) {
            String sql = ((LiteralPart) multiPart.part(0)).getSql();

            if (ParserUtils.inlineComment.matcher(sql).matches()) return null;

            return new StaticEql(sql);
        }

        return new DynamicEql(multiPart);
    }


    private List<String> standardLines(List<String> oneSqlLines) {
        return rearrangeLinesForInlineComments(rearrangeLinesForLineCommentsAndOthers(oneSqlLines));
    }

    private List<String> rearrangeLinesForInlineComments(List<String> lines) {
        List<String> convertedLines = Lists.newArrayList();

        StringBuilder mergedLine = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith("--")) {
                mergedAdd(convertedLines, mergedLine, line);
                continue;
            }

            // splits /* */ to seperate lines
            Matcher matcher = ParserUtils.inlineComment.matcher(line);
            int lastStart = 0;
            while (matcher.find()) {
                int start = matcher.start();
                if (start > lastStart) {
                    mergeLine(mergedLine, line.substring(lastStart, start), false);
                }

                lastStart = matcher.end();
                mergedAdd(convertedLines, mergedLine, matcher.group());
            }

            if (lastStart < line.length()) mergeLine(mergedLine, line.substring(lastStart), true);
            else if (lastStart > 0) mergedLine.append('\n');
        }

        if (mergedLine.length() > 0) convertedLines.add(mergedLine.toString());

        return convertedLines;
    }

    private List<String> rearrangeLinesForLineCommentsAndOthers(List<String> oneSqlLines) {
        List<String> convertedLines = Lists.newArrayList();

        StringBuilder mergedLine = new StringBuilder();
        for (String line : oneSqlLines) {
            if (line.startsWith("--")) {
                mergedAdd(convertedLines, mergedLine, line);
            } else {
                mergeLine(mergedLine, line, true);
            }
        }

        if (mergedLine.length() > 0) convertedLines.add(mergedLine.toString());
        return convertedLines;
    }

    private void mergedAdd(List<String> merged, StringBuilder mergedLine, String line) {
        if (mergedLine.length() > 0) {
            merged.add(mergedLine.toString());
            mergedLine.delete(0, mergedLine.length());
        }
        merged.add(line);
    }


    private void mergeLine(StringBuilder mergedLine, String line, boolean newLine) {
        String trim = line.trim();
        if (trim.length() == 0) return;

        String trim1 = mergeEndLfs(trimButLf(line));
        mergedLine.append(trim1);
        if (newLine && isLastCharNotLf(trim1)) mergedLine.append('\n');
    }

    private String mergeEndLfs(String line) {
        int to = line.length() - 1;
        while (to > 0 && to - 1 >= 0) {
            char c1 = line.charAt(to);
            char c2 = line.charAt(to - 1);
            if (c1 == c2 && c2 == '\n') --to;
            else break;
        }

        return line.substring(0, to + 1);
    }

    private boolean isLastCharNotLf(String str) {
        return str.charAt(str.length() - 1) != '\n';
    }

    private String trimButLf(String line) {
        int from = 0;
        int length = line.length();
        while (from < length) {
            char c = line.charAt(from);
            if (Character.isWhitespace(c)) ++from;
            else break;
        }

        int to = length - 1;
        while (to >= 0) {
            char c = line.charAt(to);
            if (Character.isWhitespace(c) && c != '\n') --to;
            else break;
        }

        return line.substring(from, to + 1);
    }

}
