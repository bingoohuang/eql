package org.n3r.eql.param;

import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import lombok.val;
import org.n3r.eql.ex.EqlConfigException;
import org.n3r.eql.impl.EqlUniqueSqlId;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.map.EqlType;
import org.n3r.eql.param.EqlParamPlaceholder.InOut;
import org.n3r.eql.util.S;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EqlParamsParser {
    private static Pattern PARAM_PATTERN = Pattern.compile("'?#(.*?)#'?");
    public final static char SUB = (char) 26; // 0001 1010 26 1A SUB (substitute) 替补
    public final static char DC1 = (char) 17; // 0001 0001 17 11 DC1 (device control 1) 设备控制1
    public final static char DC2 = (char) 18;// 0001 0010 18 12 DC2 (device control 2) 设备控制2

    static LoadingCache<EqlUniqueSqlTemplate, EqlParamsParserResult> cache;

    static {
        cache = CacheBuilder.newBuilder().build(
                new CacheLoader<EqlUniqueSqlTemplate, EqlParamsParserResult>() {
                    @Override
                    public EqlParamsParserResult load(EqlUniqueSqlTemplate eqlUniquEQLTemplate) throws Exception {
                        val result = new EqlParamsParserResult();
                        new EqlParamsParser(result, eqlUniquEQLTemplate).parseParams();
                        return result;
                    }
                }
        );
    }

    public static void parseParams(EqlRun eqlRun, String sqlStr) {
        val eqlBlock = eqlRun.getEqlBlock();
        val uniqueSqlId = eqlBlock != null ? eqlBlock.getUniqueSqlId() : null;
        val sqlTemplate = new EqlUniqueSqlTemplate(uniqueSqlId, sqlStr);

        val result = cache.getUnchecked(sqlTemplate);

        eqlRun.setSqlType(result.getSqlType());
        eqlRun.setRunSql(result.getRunSql());
        eqlRun.setEvalSqlTemplate(result.getEvalSqlTemplate());
        eqlRun.setPlaceholderNum(result.getPlaceholderNum());
        eqlRun.setPlaceHolderType(result.getPlaceHolderType());
        eqlRun.setPlaceHolderOutType(result.getPlaceHolderOutType());
        eqlRun.setPlaceHolders(result.getPlaceHolders());
    }

    private final EqlParamsParserResult result;
    private final EqlUniqueSqlId eqlUniqueSQLId;
    private final String templateSQL;

    public EqlParamsParser(EqlParamsParserResult result,
                           EqlUniqueSqlTemplate eqlUniquEQLTemplate) {
        this.result = result;
        this.eqlUniqueSQLId = eqlUniquEQLTemplate.getEqlUniquEQLId();
        this.templateSQL = eqlUniquEQLTemplate.getTemplatEQL();
    }

    private void parseParams() {
        EqlType eqlType = EqlType.parseSqlType(templateSQL);
        result.setSqlType(eqlType);

        Matcher matcher = PARAM_PATTERN.matcher(templateSQL);
        List<String> placeHolders = new ArrayList<String>();
        List<String> placeHolderOptions = new ArrayList<String>();
        StringBuilder sql = new StringBuilder();
        StringBuilder evalSql = new StringBuilder();
        int questionSeq = -1;
        int startPos = 0;
        boolean hasEscape = false;
        while (matcher.find(startPos)) {
            if (hasPrevEscape(matcher.start())) {
                String prev = templateSQL.substring(startPos, matcher.start() + 1);
                sql.append(prev);
                evalSql.append(prev);
                startPos = matcher.start() + 1;
                hasEscape = true;
                continue;
            }

            String placeHolder = matcher.group(1).trim();
            String paramOptions = "";
            int colonPos = placeHolder.indexOf(':');
            if (colonPos >= 0) {
                paramOptions = placeHolder.substring(colonPos + 1).trim();
                placeHolder = placeHolder.substring(0, colonPos).trim();
            }

            if ("?".equals(placeHolder))
                placeHolder = inferVarName(eqlType, templateSQL, startPos, matcher.start());

            placeHolders.add(S.unEscapeCrossAndDollar(placeHolder));
            placeHolderOptions.add(paramOptions);

            String prev = templateSQL.substring(startPos, matcher.start());
            sql.append(prev).append('?');
            evalSql.append(prev).append(S.wrap(++questionSeq, SUB));
            startPos = matcher.end();
        }

        String tail = templateSQL.substring(startPos);
        sql.append(tail);
        evalSql.append(tail);

        String oneLinEQL = sql.toString();
        String oneLineEvalSql = evalSql.toString();

        result.setRunSql(hasEscape ? unescape(oneLinEQL) : oneLinEQL);
        result.setEvalSqlTemplate(hasEscape ? unescape(oneLineEvalSql) : oneLineEvalSql);

        result.setPlaceholderNum(placeHolders.size());

        parsePlaceholders(placeHolders, placeHolderOptions);
    }

    private String unescape(String sql) {
        StringBuilder unescape = new StringBuilder(sql.length());
        int lastPos = 0;
        for (int pos = sql.indexOf('\\', lastPos); pos >= 0 && lastPos < sql.length(); ) {
            unescape.append(sql.substring(lastPos, pos)).append(sql.charAt(pos + 1));
            lastPos = pos + 2;
            if (lastPos < sql.length()) pos = sql.indexOf('\\', lastPos);
        }

        if (lastPos < sql.length()) unescape.append(sql.substring(lastPos));

        return unescape.toString();
    }

    private boolean hasPrevEscape(int start) {
        if (start == 0) return false;

        boolean hasPreEscape = false;
        for (int i = start - 1; i >= 0; --i, hasPreEscape = !hasPreEscape)
            if (templateSQL.charAt(i) != '\\') break;

        return hasPreEscape;
    }

    private static final Pattern lastWord = Pattern.compile(".*\\b([\\w_\\d]+)\\b.*$", Pattern.DOTALL);
    private static final Pattern questionPattern = Pattern.compile("'?#\\s*\\?\\s*(:.*)?\\s*#'?");

    private String inferVarName(EqlType sqlType, String rawSql, int startPos, int endPos) {
        String variableName = null;
        switch (sqlType) {
            case SELECT:
            case UPDATE:
                variableName = inferVarNameInUpdateSQL(rawSql, startPos, endPos);
                break;
            case INSERT:
            case REPLACE:
                variableName = inferVarNameInInsertSql(rawSql, endPos);
                break;
            case MERGE:
                variableName = inferVarNameInMergEQL(rawSql, startPos, endPos);
                break;
            default:
                break;
        }

        if (variableName == null)
            throw new EqlConfigException("unable to parse #?# in " + rawSql);

        return variableName;
    }

    private String inferVarNameInMergEQL(String rawSql, int startPos, int endPos) {
        String upperCaseRawSql = rawSql.toUpperCase();
        int updatePos = upperCaseRawSql.indexOf("UPDATE");
        int insertPos = upperCaseRawSql.indexOf("INSERT");
        if (updatePos < 0 && insertPos >= 0)
            return inferVarNameInInsertSql(rawSql, endPos);

        if (updatePos >= 0 && insertPos < 0)
            return inferVarNameInUpdateSQL(rawSql, startPos, endPos);

        int minPos = Math.min(updatePos, insertPos);
        if (minPos == updatePos && endPos < insertPos || minPos == insertPos && endPos > updatePos)
            return inferVarNameInUpdateSQL(rawSql, startPos, endPos);
        if (minPos == updatePos && endPos > insertPos || minPos == insertPos && endPos < updatePos)
            return inferVarNameInInsertSql(rawSql, endPos);

        return null;
    }

    private String inferVarNameInInsertSql(String rawSql, int endPos) {
        int insertPos = rawSql.toUpperCase().indexOf("INSERT");
        int leftBracket = rawSql.indexOf('(', insertPos + 1);
        int rightBracket = rawSql.indexOf(')', leftBracket + 1);
        if (leftBracket < 0 || rightBracket < 0) return null;

        // 分割insert into xxxtable(a,b,c,d)中的字段列表部分(括弧内)
        String fieldsStr = rawSql.substring(leftBracket + 1, rightBracket);
        Splitter splitter = Splitter.on(',').trimResults().omitEmptyStrings();
        String[] fields = Iterables.toArray(splitter.split(fieldsStr), String.class);
        // 计算当前#?#是第几个
        int valuePos = rawSql.toUpperCase().indexOf("VALUES");

        String valuesPart = S.substrInQuotes(rawSql, '(', valuePos + "VALUES".length());
        int leftBracketPos = rawSql.indexOf('(', valuePos + "VALUES".length());
        String parseLeft = rawSql.substring(leftBracketPos + 1, endPos);

        String[] leftValues = Iterables.toArray(splitter.split(parseLeft), String.class);
        String[] values = Iterables.toArray(splitter.split(valuesPart), String.class);
        for (int i = leftValues.length; i < fields.length; ++i) {
            Matcher matcher = questionPattern.matcher(values[i]);
            if (matcher.matches()) return fields[i];
        }

        return null;
    }

    private String inferVarNameInUpdateSQL(String rawSql, int startPos, int endPos) {
        String substr = rawSql.substring(startPos, endPos);
        Matcher matcher = lastWord.matcher(substr);
        if (!matcher.matches())
            throw new EqlConfigException("Unable to resolve #?#： " + substr);

        return matcher.group(1);
    }

    private void parsePlaceholders(List<String> placeHolders, List<String> placeHolderOptions) {
        List<EqlParamPlaceholder> placeholders = new ArrayList<EqlParamPlaceholder>();

        for (int i = 0, ii = placeHolders.size(); i < ii; ++i) {
            String placeHolderStr = placeHolders.get(i);
            String placeHolderOption = placeHolderOptions.get(i);

            val placeholder = new EqlParamPlaceholder();
            placeholders.add(placeholder);
            placeholder.setPlaceholder(placeHolderStr);
            placeholder.parseOption(placeHolderOption);

            if (placeHolderStr.length() == 0) {
                placeholder.setPlaceholderType(PlaceholderType.AUTO_SEQ);
            } else if (S.isInteger(placeHolderStr)) {
                placeholder.setPlaceholderType(PlaceholderType.MANU_SEQ);
                placeholder.setSeq(Integer.parseInt(placeHolderStr, 10));
            } else placeholder.setPlaceholderType(PlaceholderType.VAR_NAME);
        }

        result.setPlaceHolderType(setAndCheckPlaceholderInType(placeholders, InOut.OUT));
        result.setPlaceHolderOutType(setAndCheckPlaceholderInType(placeholders, InOut.IN));
        if (result.getPlaceHolderType() == PlaceholderType.UNSET)
            result.setPlaceHolderType(result.getPlaceHolderOutType());

        result.setPlaceHolders(placeholders.toArray(new EqlParamPlaceholder[0]));
    }

    private PlaceholderType setAndCheckPlaceholderInType(
            List<EqlParamPlaceholder> paramPlaceholders, InOut inOut) {
        PlaceholderType holderType = PlaceholderType.UNSET;
        for (EqlParamPlaceholder paramPlaceholder : paramPlaceholders)
            if (holderType != paramPlaceholder.getPlaceholderType()
                    && paramPlaceholder.getInOut() != inOut) {
                if (holderType != PlaceholderType.UNSET)
                    throw new EqlConfigException("[" + (eqlUniqueSQLId != null ? eqlUniqueSQLId.getSqlId() : templateSQL)
                            + "] with different param binding types");

                holderType = paramPlaceholder.getPlaceholderType();
            }

        if (holderType == PlaceholderType.MANU_SEQ && result.getSqlType() == EqlType.CALL)
            throw new EqlConfigException("["
                    + (eqlUniqueSQLId != null ? eqlUniqueSQLId.getSqlId() : templateSQL) + "] is a procedure without manually bind seq support");

        return holderType;
    }

}
