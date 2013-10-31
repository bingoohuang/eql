package org.n3r.eql.parser;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.n3r.eql.impl.SqlResourceLoader;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlParser {
    private static Pattern blockPattern = Pattern.compile("\\[\\s*([\\w\\.\\-\\d]+)\\b(.*)\\]");

    public static Map<String, SqlBlock> parse(String sqlClassPath, String str) {
        Iterable<String> lines = Splitter.on('\n').trimResults().split(str);

        SqlBlock block = null;
        Map<String, SqlBlock> blocks = Maps.newHashMap();
        List<String> sqlLines = null;

        int lineNo = 0;
        for (String line : lines) {
            ++lineNo;

            line = line.trim();
            if (line.length() == 0) continue;

            if (line.startsWith("--")) {
                String cleanLine = ParserUtils.substr(line, "--".length());
                if (importOtherSqlFile(blocks, cleanLine, sqlClassPath)) continue;

                Matcher matcher = blockPattern.matcher(cleanLine);
                if (matcher.matches()) { // new sql block found
                    parseBlock(block, sqlLines);
                    block = new SqlBlock(sqlClassPath, matcher.group(1), matcher.group(2), lineNo);
                    sqlLines = Lists.newArrayList();
                    addBlock(sqlClassPath, blocks, block);
                    continue;
                }
            }

            if (block == null) continue; // without any block, just ignore
            sqlLines.add(line);
        }

        parseBlock(block, sqlLines);

        return blocks;
    }

    static Pattern loadPattern = Pattern.compile("import\\s+([/.\\w]+)(\\s+.*)?", Pattern.CASE_INSENSITIVE);

    private static boolean importOtherSqlFile(Map<String, SqlBlock> blocks, String cleanLine, String sqlClassPath) {
        Matcher matcher = loadPattern.matcher(cleanLine);
        if (!matcher.matches()) return false;

        String classPath = matcher.group(1).trim();
        String patterns = ParserUtils.trim(matcher.group(2));

        if (classPath.equals(sqlClassPath)) return true;

        Map<String, SqlBlock> importRes = SqlResourceLoader.load(classPath);
        if (ParserUtils.isBlank(patterns)) {
            importSqlBlocks(sqlClassPath, blocks, cleanLine, importRes);
            return true;
        }

        Map<String, SqlBlock> temp = Maps.newHashMap();
        for (String pattern : Splitter.onPattern("\\s+")
                .omitEmptyStrings().trimResults().split(patterns)) {
            for (SqlBlock sqlBlock : importRes.values()) {
                if (wildCardMatch(sqlBlock.getSqlId(), pattern)) {
                    temp.put(sqlBlock.getSqlId(), sqlBlock);
                }
            }
        }

        importSqlBlocks(sqlClassPath, blocks, cleanLine, temp);

        return true;
    }

    private static void importSqlBlocks(String sqlClassPath, Map<String, SqlBlock> blocks,
                                        String cleanLine, Map<String, SqlBlock> temp) {
        for (SqlBlock sqlBlock : temp.values()) {
            if (blocks.containsKey(sqlBlock.getSqlId())) {
                throw new RuntimeException(sqlBlock.getSqlId() + " deplicated when " + cleanLine + " in " + sqlClassPath);
            }

            blocks.put(sqlBlock.getSqlId(), sqlBlock);
        }
    }

    private static void addBlock(String sqlClassPath, Map<String, SqlBlock> blocks, SqlBlock sqlBlock) {
        if (blocks.containsKey(sqlBlock.getSqlId())) {
            throw new RuntimeException(sqlBlock.getSqlId() + " deplicated in " + sqlClassPath);
        }

        blocks.put(sqlBlock.getSqlId(), sqlBlock);
    }


    private static void parseBlock(SqlBlock block, List<String> sqlLines) {
        if (block != null && sqlLines != null) {
            block.parseBlock(sqlLines);
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
