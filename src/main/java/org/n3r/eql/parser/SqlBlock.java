package org.n3r.eql.parser;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.n3r.eql.param.EqlParamsParser;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.EqlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class SqlBlock{
    private final int startLineNo;
    private final String sqlClassPath;
    private String sqlId;
    private Map<String, String> options = Maps.newHashMap();
    private Class<?> returnType;
    private String onerr;
    private String split;

    private List<Sql> sqls = Lists.newArrayList();

    public SqlBlock(String sqlClassPath, String sqlId, String options, int startLineNo) {
        this.sqlClassPath = sqlClassPath;
        this.sqlId = sqlId;
        this.startLineNo = startLineNo;
        this.options = BlockOptionsParser.parseOptions(options);

        initSomeOptions();
    }

    private void initSomeOptions() {
        onerr = options.get("onerr");
        returnType = EqlUtils.tryLoadClass(options.get("returnType"));

        split = options.get("split");
        if (Strings.isNullOrEmpty(split)) split = ";";
    }

    public String getSqlId() {
        return sqlId;
    }

    public List<Sql> getSqls() {
        return sqls;
    }

    public void parseBlock(List<String> sqlLines) {
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
    }

    private void addSql(List<String> oneSqlLines) {
        if (oneSqlLines.size() == 0) return;

        Sql sql = parseSql(oneSqlLines);
        if (sql != null) sqls.add(sql);
        oneSqlLines.clear();
    }



    private Sql parseSql(List<String> oneSqlLines) {
        List<String> mergedLines = mergeLines(oneSqlLines);

        MultiPart sqlParts = new MultiPart();

        for (int i = 0, ii = mergedLines.size(); i < ii; ++i) {
            String line = mergedLines.get(i);

            if (line.startsWith("--")) {
                String clearLine = ParserUtils.substr(line, "--".length());
                PartParser partParser = PartParserFactory.tryParse(clearLine);
                if (partParser != null) {
                    i = partParser.parse(mergedLines, i + 1) - 1;
                    sqlParts.addPart(partParser.createPart());
                } else {
                    sqlParts.addPart(new LiteralPart(line));
                }

            } else {
                Matcher matcher = ParserUtils.inlineComment.matcher(line);
                if (!matcher.matches()) {
                    sqlParts.addPart(new LiteralPart(line));
                    continue;
                }

                String cleanStr = matcher.group(1);

                PartParser partParser = PartParserFactory.tryParse(cleanStr);
                if (partParser != null) {
                    i = partParser.parse(mergedLines, i + 1) - 1;
                    sqlParts.addPart(partParser.createPart());
                } else {
                    sqlParts.addPart(new LiteralPart(line));
                }
            }
        }

        if (sqlParts.size() == 1 && sqlParts.part(0) instanceof LiteralPart) {
            String sql = Joiner.on(' ').join(oneSqlLines).trim();

            if (sql.startsWith("--")) return null;
            if (ParserUtils.inlineComment.matcher(sql).matches()) return null;

            StaticSql staticSql = new StaticSql();
            staticSql.setSql(sql);

            return staticSql;
        }

        return new DynamicSql(sqlParts);
    }

    private List<String> mergeLines(List<String> oneSqlLines) {
        List<String> merged = Lists.newArrayList();

        StringBuilder mergedLine = new StringBuilder();
        for (String line : oneSqlLines) {
            if (line.startsWith("--")) {
                mergedAdd(merged, mergedLine, line);
                continue;
            }

            // splits /* */ to seperate lines
            Matcher matcher = ParserUtils.inlineComment.matcher(line);
            int lastStart  = 0;
            while (matcher.find()) {
                int start = matcher.start();
                if (start > lastStart) {
                    appendSqlPart(merged, mergedLine, line.substring(lastStart, start));
                }

                lastStart = matcher.end();
                mergedAdd(merged, mergedLine, matcher.group());
            }

            if (lastStart < line.length()) appendSqlPart(merged, mergedLine, line.substring(lastStart));
        }

        if (mergedLine.length() > 0) merged.add(mergedLine.toString());

        return merged;
    }

    private void mergedAdd(List<String> merged, StringBuilder mergedLine, String line) {
        if (mergedLine.length() > 0) {
            merged.add(mergedLine.toString());
            mergedLine.delete(0, mergedLine.length());
        }
        merged.add(line);
    }

    private void appendSqlPart(List<String> merged, StringBuilder mergedLine, String line) {
        String trim = line.trim();
        if (trim.length()  == 0) return;

        if (trim.startsWith("--")) {
            if (mergedLine.length() > 0) {
                merged.add(mergedLine.toString());
                mergedLine.delete(0, mergedLine.length());
            }

            merged.add(trim);
            return;
        }

        if (mergedLine.length() > 0) mergedLine.append(' ');
        mergedLine.append(trim);
    }

    public List<EqlRun> createSqlSubs(Object... params) {
        Object bean =  EqlUtils.compositeParams(params);

        ArrayList<EqlRun> sqlSubs = new ArrayList<EqlRun>();
        EqlRun lastSelectSql = null;
        for (Sql sql : sqls) {
            String sqlStr = sql.evalSql(bean);
            if (Strings.isNullOrEmpty(sqlStr)) continue;

            EqlRun eqlRun = new EqlParamsParser().parseParams(sqlStr, this);
            sqlSubs.add(eqlRun);
            if (eqlRun.getSqlType() == EqlRun.EqlType.SELECT) lastSelectSql = eqlRun;
        }

        if (lastSelectSql != null) lastSelectSql.setLastSelectSql(true);

        return sqlSubs;
    }

    public boolean isOnerrResume() {
        return "resume".equalsIgnoreCase(onerr);
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public String getSqlClassPath() {
        return sqlClassPath;
    }
}
