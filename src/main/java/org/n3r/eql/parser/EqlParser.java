package org.n3r.eql.parser;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.n3r.eql.base.DynamicLanguageDriver;
import org.n3r.eql.base.EqlResourceLoader;
import org.n3r.eql.impl.DefaultDynamicLanguageDriver;
import org.n3r.eql.settings.EqlFileGlobalSettings;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse a whole eql file.
 */
public class EqlParser {
    static Pattern blockPattern = Pattern.compile("\\[\\s*([\\w\\.\\-\\d]+)\\b(.*)\\]");

    private Map<String, EqlBlock> blocks = Maps.newHashMap();
    private String sqlClassPath;
    private List<String> sqlLines = null;
    private DynamicLanguageDriver dynamicLanguageDriver;

    private EqlBlock block = null;
    private EqlResourceLoader eqlResourceLoader;
    private boolean sqlParseDelay;

    public EqlParser(EqlResourceLoader eqlResourceLoader, String sqlClassPath) {
        this.eqlResourceLoader = eqlResourceLoader;
        this.sqlClassPath = sqlClassPath;
        if (eqlResourceLoader != null)
            this.dynamicLanguageDriver = eqlResourceLoader.getDynamicLanguageDriver();

        if (dynamicLanguageDriver == null)
            dynamicLanguageDriver = new DefaultDynamicLanguageDriver();
    }

    // delay sql parse
    public Map<String, EqlBlock> delayParse(String eqlStr) {
        this.sqlParseDelay = true;
        return parse(eqlStr);
    }

    public Map<String, EqlBlock> parse(String eqlStr) {
        Iterable<String> lines = Splitter.on('\n').trimResults().split(eqlStr);

        int lineNo = 0;
        for (String line : lines) {
            ++lineNo;

            line = line.trim();
            if (line.length() == 0) continue;

            if (line.startsWith("--")) {
                String cleanLine = ParserUtils.substr(line, "--".length());
                if (importOtherSqlFile(cleanLine)) continue;
                if (includeOtherSqlId(cleanLine)) continue;
                if (globalSettings(cleanLine)) continue;

                Matcher matcher = blockPattern.matcher(cleanLine);
                if (matcher.matches()) { // new sql block found
                    parsePreviousBlock();
                    String sqlId = matcher.group(1);
                    String options = matcher.group(2);
                    block = new EqlBlock(sqlClassPath, sqlId, options, lineNo);
                    addBlock(block);
                    continue;
                }
            }

            if (block == null) continue; // without any block, just ignore
            sqlLines.add(line);
        }

        parsePreviousBlock();

        return blocks;
    }

    static Pattern globalSettingsPattern = Pattern.compile("global\\s+settings\\s+(.+)");

    private boolean globalSettings(String cleanLine) {
        Matcher matcher = globalSettingsPattern.matcher(cleanLine);
        if (!matcher.matches()) return false;

        String globalSettings = matcher.group(1).trim();

        EqlFileGlobalSettings.process(sqlClassPath, globalSettings);

        return true;
    }

    static Pattern includePattern = Pattern.compile("(include|ref)\\s+([\\w\\.\\-\\d]+)");

    private boolean includeOtherSqlId(String cleanLine) {
        Matcher matcher = includePattern.matcher(cleanLine);
        if (!matcher.matches()) return false;

        if (block == null) return true;

        String includEQLId = matcher.group(2);
        EqlBlock eqlBlock = blocks.get(includEQLId);
        if (eqlBlock == null) throw new RuntimeException(cleanLine + " not found");
        sqlLines.addAll(eqlBlock.getSqlLines());

        String ref = matcher.group(1);
        if (!"ref".equals(ref)) sqlLines.add(";");

        return true;
    }

    static Pattern importPattern = Pattern.compile("import\\s+([/.\\w]+)(\\s+.*)?");

    private boolean importOtherSqlFile(String cleanLine) {
        Matcher matcher = importPattern.matcher(cleanLine);
        if (!matcher.matches()) return false;

        parsePreviousBlock();

        String classPath = matcher.group(1).trim();
        String patterns = ParserUtils.trim(matcher.group(2));

        if (classPath.equals(sqlClassPath)) return true;

        Map<String, EqlBlock> importRes = eqlResourceLoader.load(classPath);
        if (ParserUtils.isBlank(patterns)) {
            importSqlBlocks(cleanLine, importRes);
            return true;
        }

        Map<String, EqlBlock> temp = Maps.newHashMap();
        Splitter splitter = Splitter.onPattern("\\s+").omitEmptyStrings().trimResults();
        for (String pattern : splitter.split(patterns)) {
            for (EqlBlock eqlBlock : importRes.values()) {
                if (wildCardMatch(eqlBlock.getSqlId(), pattern)) {
                    temp.put(eqlBlock.getSqlId(), eqlBlock);
                }
            }
        }

        importSqlBlocks(cleanLine, temp);

        return true;
    }

    private void importSqlBlocks(String cleanLine, Map<String, EqlBlock> temp) {
        for (EqlBlock eqlBlock : temp.values()) {
            if (blocks.containsKey(eqlBlock.getSqlId())) {
                throw new RuntimeException(eqlBlock.getSqlId() + " deplicated when "
                        + cleanLine + " in " + sqlClassPath);
            }

            blocks.put(eqlBlock.getSqlId(), eqlBlock);
        }
    }

    private void addBlock(EqlBlock eqlBlock) {
        if (blocks.containsKey(eqlBlock.getSqlId()) && !eqlBlock.isOverride()) {
            throw new RuntimeException(eqlBlock.getSqlId() + " deplicated in " + sqlClassPath);
        }

        blocks.put(eqlBlock.getSqlId(), eqlBlock);
        sqlLines = Lists.newArrayList();
    }

    private void parsePreviousBlock() {
        if (block != null && sqlLines != null && sqlLines.size() > 0) {
            new EqlBlockParser(dynamicLanguageDriver, sqlParseDelay).parse(block, sqlLines);

            block = null;
            sqlLines = null;
        }
    }

    public static boolean wildCardMatch(String text, String pattern) {
        // Create the cards by splitting using a RegEx. If more speed
        // is desired, a simpler character based splitting can be done.
        String[] cards = pattern.split("\\*");

        for (String card : cards) {
            int idx = text.indexOf(card);
            if (idx == -1) return false;

            text = text.substring(idx + card.length());
        }

        return true;
    }

}
