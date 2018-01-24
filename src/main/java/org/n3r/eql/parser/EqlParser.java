package org.n3r.eql.parser;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.n3r.eql.base.DynamicLanguageDriver;
import org.n3r.eql.base.EqlResourceLoader;
import org.n3r.eql.impl.DefaultDynamicLanguageDriver;
import org.n3r.eql.settings.EqlFileGlobalSettings;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Parse a whole eql file.
 */
@Slf4j
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
            dynamicLanguageDriver = eqlResourceLoader.getDynamicLanguageDriver();

        if (dynamicLanguageDriver == null)
            dynamicLanguageDriver = new DefaultDynamicLanguageDriver();
    }

    // delay sql parse
    public Map<String, EqlBlock> delayParse(String eqlStr) {
        this.sqlParseDelay = true;
        return parse(eqlStr);
    }

    public Map<String, EqlBlock> parse(String eqlStr) {
        val lines = Splitter.on('\n').trimResults().split(eqlStr);

        int lineNo = 0;
        for (String line : lines) {
            ++lineNo;

            line = line.trim();
            if (line.length() == 0) continue;

            if (line.startsWith("--")) {
                val cleanLine = ParserUtils.substr(line, 2/*"--".length()*/);
                if (importOtherSqlFile(cleanLine)) continue;
                if (includeOtherSqlId(cleanLine)) continue;
                if (globalSettings(cleanLine)) continue;

                val matcher = blockPattern.matcher(cleanLine);
                if (matcher.matches()) { // new sql block found
                    parsePreviousBlock();
                    val sqlId = matcher.group(1);
                    val options = matcher.group(2);
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
        val matcher = globalSettingsPattern.matcher(cleanLine);
        if (!matcher.matches()) return false;

        val globalSettings = matcher.group(1).trim();

        EqlFileGlobalSettings.process(sqlClassPath, globalSettings);

        return true;
    }

    static Pattern includePattern = Pattern.compile("(include|ref)\\s+([\\w\\.\\-\\d]+)");

    private boolean includeOtherSqlId(String cleanLine) {
        val matcher = includePattern.matcher(cleanLine);
        if (!matcher.matches()) return false;

        if (block == null) return true;

        val includeEqlId = matcher.group(2);
        val eqlBlock = blocks.get(includeEqlId);
        if (eqlBlock == null) {
            log.error("include eql id {} not found in {}", includeEqlId, sqlClassPath);
            throw new RuntimeException(cleanLine + " not found");
        }
        sqlLines.addAll(eqlBlock.getSqlLines());

        val ref = matcher.group(1);
        if (!"ref".equals(ref)) sqlLines.add(";");

        return true;
    }

    static Pattern importPattern = Pattern.compile("import\\s+([/.\\w]+)(\\s+.*)?");

    private boolean importOtherSqlFile(String cleanLine) {
        val matcher = importPattern.matcher(cleanLine);
        if (!matcher.matches()) return false;

        parsePreviousBlock();

        val classPath = matcher.group(1).trim();
        val patterns = ParserUtils.trim(matcher.group(2));

        if (classPath.equals(sqlClassPath)) return true;

        val importRes = eqlResourceLoader.load(classPath);
        if (ParserUtils.isBlank(patterns)) {
            importSqlBlocks(cleanLine, importRes);
            return true;
        }

        Map<String, EqlBlock> temp = Maps.newHashMap();
        val splitter = Splitter.onPattern("\\s+").omitEmptyStrings().trimResults();
        for (val pattern : splitter.split(patterns)) {
            for (val eqlBlock : importRes.values()) {
                if (wildCardMatch(eqlBlock.getSqlId(), pattern)) {
                    temp.put(eqlBlock.getSqlId(), eqlBlock);
                }
            }
        }

        importSqlBlocks(cleanLine, temp);

        return true;
    }

    private void importSqlBlocks(String cleanLine, Map<String, EqlBlock> temp) {
        for (val eqlBlock : temp.values()) {
            if (blocks.containsKey(eqlBlock.getSqlId())) {
                log.error("{} duplicated when {} in {}",
                        eqlBlock.getSqlId(), cleanLine, sqlClassPath);
                throw new RuntimeException(eqlBlock.getSqlId()
                        + " duplicated when " + cleanLine + " in " + sqlClassPath);
            }

            blocks.put(eqlBlock.getSqlId(), eqlBlock);
        }
    }

    private void addBlock(EqlBlock eqlBlock) {
        if (blocks.containsKey(eqlBlock.getSqlId()) && !eqlBlock.isOverride()) {
            log.error("{} duplicated in {}", eqlBlock.getSqlId(), sqlClassPath);
            throw new RuntimeException(eqlBlock.getSqlId() + " duplicated in " + sqlClassPath);
        }

        blocks.put(eqlBlock.getSqlId(), eqlBlock);
        sqlLines = Lists.<String>newArrayList();
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
        val cards = pattern.split("\\*");

        for (val card : cards) {
            int idx = text.indexOf(card);
            if (idx == -1) return false;

            text = text.substring(idx + card.length());
        }

        return true;
    }
}
