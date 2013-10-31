package org.n3r.eql.parser;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.n3r.eql.impl.SqlResourceLoader;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EqlParser {
    static Pattern blockPattern = Pattern.compile("\\[\\s*([\\w\\.\\-\\d]+)\\b(.*)\\]");

    private Map<String, SqlBlock> blocks = Maps.newHashMap();
    private String sqlClassPath;
    private List<String> sqlLines = null;

    private SqlBlock block = null;

    public Map<String, SqlBlock> parse(String sqlClassPath, String str) {
        this.sqlClassPath = sqlClassPath;
        Iterable<String> lines = Splitter.on('\n').trimResults().split(str);

        int lineNo = 0;
        for (String line : lines) {
            ++lineNo;

            line = line.trim();
            if (line.length() == 0) continue;

            if (line.startsWith("--")) {
                String cleanLine = ParserUtils.substr(line, "--".length());
                if (importOtherSqlFile(cleanLine)) continue;
                if (includeOtherSqlId(cleanLine)) continue;

                Matcher matcher = blockPattern.matcher(cleanLine);
                if (matcher.matches()) { // new sql block found
                    parseBlock();
                    block = new SqlBlock(sqlClassPath, matcher.group(1), matcher.group(2), lineNo);
                    addBlock(block);
                    continue;
                }
            }

            if (block == null) continue; // without any block, just ignore
            sqlLines.add(line);
        }

        parseBlock();

        return blocks;
    }

    static Pattern includePattern = Pattern.compile("include\\s+([\\w\\.\\-\\d]+)",
            Pattern.CASE_INSENSITIVE);
    private boolean includeOtherSqlId(String cleanLine) {
        Matcher matcher = includePattern.matcher(cleanLine);
        if (!matcher.matches()) return false;

        if (block == null) return true;

        String includeSqlId = matcher.group(1);
        SqlBlock sqlBlock = blocks.get(includeSqlId);
        if (sqlBlock == null) throw new RuntimeException(cleanLine + " not found");
        sqlLines.addAll(sqlBlock.getSqlLines());

        return true;
    }

    static Pattern importPattern = Pattern.compile("import\\s+([/.\\w]+)(\\s+.*)?",
            Pattern.CASE_INSENSITIVE);

    private boolean importOtherSqlFile(String cleanLine) {
        Matcher matcher = importPattern.matcher(cleanLine);
        if (!matcher.matches()) return false;

        parseBlock();

        String classPath = matcher.group(1).trim();
        String patterns = ParserUtils.trim(matcher.group(2));

        if (classPath.equals(sqlClassPath)) return true;

        Map<String, SqlBlock> importRes = SqlResourceLoader.load(classPath);
        if (ParserUtils.isBlank(patterns)) {
            importSqlBlocks(cleanLine, importRes);
            return true;
        }

        Map<String, SqlBlock> temp = Maps.newHashMap();
        Splitter splitter = Splitter.onPattern("\\s+").omitEmptyStrings().trimResults();
        for (String pattern : splitter.split(patterns)) {
            for (SqlBlock sqlBlock : importRes.values()) {
                if (wildCardMatch(sqlBlock.getSqlId(), pattern)) {
                    temp.put(sqlBlock.getSqlId(), sqlBlock);
                }
            }
        }

        importSqlBlocks(cleanLine, temp);

        return true;
    }

    private void importSqlBlocks(String cleanLine, Map<String, SqlBlock> temp) {
        for (SqlBlock sqlBlock : temp.values()) {
            if (blocks.containsKey(sqlBlock.getSqlId())) {
                throw new RuntimeException(sqlBlock.getSqlId() + " deplicated when " + cleanLine + " in " + sqlClassPath);
            }

            blocks.put(sqlBlock.getSqlId(), sqlBlock);
        }
    }

    private void addBlock(SqlBlock sqlBlock) {
        if (blocks.containsKey(sqlBlock.getSqlId())) {
            throw new RuntimeException(sqlBlock.getSqlId() + " deplicated in " + sqlClassPath);
        }

        blocks.put(sqlBlock.getSqlId(), sqlBlock);
        sqlLines = Lists.newArrayList();
    }

    private void parseBlock() {
        if (block != null && sqlLines != null && sqlLines.size() > 0) {
            block.parseBlock(sqlLines);
            block = null;
            sqlLines = null;
        }
    }

    public static boolean wildCardMatch(String text, String pattern) {
        // Create the cards by splitting using a RegEx. If more speed
        // is desired, a simpler character based splitting can be done.
        String[] cards = pattern.split("\\*");

        // Iterate over the cards.
        for (String card : cards) {
            int idx = text.indexOf(card);

            // Card not detected in the text.
            if (idx == -1) {
                return false;
            }

            // Move ahead, towards the right of the text.
            text = text.substring(idx + card.length());
        }

        return true;
    }

}
