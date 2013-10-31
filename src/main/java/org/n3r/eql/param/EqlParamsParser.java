package org.n3r.eql.param;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.n3r.eql.ex.EqlConfigException;
import org.n3r.eql.parser.SqlBlock;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.EqlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EqlParamsParser {
    private static Pattern PARAM_PATTERN = Pattern.compile("#(.*?)#");
    private SqlBlock sqlBlock;
    private String templateSql;
    private EqlRun eqlRun;

    public EqlRun parseParams(String templateSql, SqlBlock sqlBlock) {
        this.sqlBlock = sqlBlock;
        this.templateSql = templateSql;

        eqlRun = new EqlRun();
        eqlRun.setSqlType(EqlUtils.parseSqlType(templateSql));

        Matcher matcher = PARAM_PATTERN.matcher(templateSql);
        List<String> placeHolders = new ArrayList<String>();
        List<String> placeHolderOptions = new ArrayList<String>();
        StringBuilder sql = new StringBuilder();
        int startPos = 0;
        boolean hasEscape = false;
        while (matcher.find(startPos)) {
            if (hasPrevEscape(matcher.start())) {
                sql.append(templateSql.substring(startPos, matcher.start() + 1));
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
                placeHolder = inferVarName(eqlRun.getSqlType(), templateSql, startPos, matcher.start());

            placeHolders.add(placeHolder);
            placeHolderOptions.add(paramOptions);

            sql.append(templateSql.substring(startPos, matcher.start())).append('?');
            startPos = matcher.end();
        }

        sql.append(templateSql.substring(startPos));
        String onelineSql = sql.toString();
        eqlRun.setRunSql(hasEscape ? unescape(onelineSql) : onelineSql);
        eqlRun.setSqlBlock(sqlBlock);
        eqlRun.setPlaceholderNum(placeHolders.size());

        parsePlaceholders(placeHolders, placeHolderOptions);

        return eqlRun;
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
            if (templateSql.charAt(i) != '\\') break;

        return hasPreEscape;
    }

    private static final Pattern lastWord = Pattern.compile(".*\\b([\\w_\\d]+)\\b.*$", Pattern.DOTALL);
    private static final Pattern questionPattern = Pattern.compile("#\\s*\\?\\s*(:.*)?\\s*#");

    private String inferVarName(EqlRun.EqlType sqlType, String rawSql, int startPos, int endPos) {
        String variableName = null;
        switch (sqlType) {
            case SELECT:
            case UPDATE:
                variableName = inferVarNameInUpdateSql(rawSql, startPos, endPos);
                break;
            case INSERT:
                variableName = inferVarNameInInsertSql(rawSql, endPos);
                break;
            case MERGE:
                variableName = inferVarNameInMergeSql(rawSql, startPos, endPos);
                break;
            default:
                break;
        }

        if (variableName == null) throw new EqlConfigException("unable to parse #?# in " + rawSql);

        return variableName;
    }

    private String inferVarNameInMergeSql(String rawSql, int startPos, int endPos) {
        String upperCaseRawSql = rawSql.toUpperCase();
        int updatePos = upperCaseRawSql.indexOf("UPDATE");
        int insertPos = upperCaseRawSql.indexOf("INSERT");
        if (updatePos < 0 && insertPos >= 0)
            return inferVarNameInInsertSql(rawSql, endPos);

        if (updatePos >= 0 && insertPos < 0)
            return inferVarNameInUpdateSql(rawSql, startPos, endPos);

        int minPos = Math.min(updatePos, insertPos);
        if (minPos == updatePos && endPos < insertPos || minPos == insertPos && endPos > updatePos)
            return inferVarNameInUpdateSql(rawSql, startPos, endPos);
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

        String valuesPart = EqlUtils.substrInQuotes(rawSql, '(', valuePos + "VALUES".length());
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

    private String inferVarNameInUpdateSql(String rawSql, int startPos, int endPos) {
        String substr = rawSql.substring(startPos, endPos);
        Matcher matcher = lastWord.matcher(substr);
        if (!matcher.matches()) throw new EqlConfigException("无法解析#?#： " + substr);

        return matcher.group(1);
    }

    private void parsePlaceholders(List<String> placeHolders, List<String> placeHolderOptions) {
        List<EqlParamPlaceholder> paramPlaceholders = new ArrayList<EqlParamPlaceholder>();

        for (int i = 0, ii = placeHolders.size(); i < ii; ++i) {
            String placeHolder = placeHolders.get(i);
            String placeHolderOption = placeHolderOptions.get(i);

            EqlParamPlaceholder paramPlaceholder = new EqlParamPlaceholder();
            paramPlaceholders.add(paramPlaceholder);
            paramPlaceholder.setPlaceholder(placeHolder);
            paramPlaceholder.setOption(placeHolderOption);

            if (placeHolder.length() == 0) paramPlaceholder.setPlaceholderType(PlaceholderType.AUTO_SEQ);
            else if (EqlUtils.isInteger(placeHolder)) {
                paramPlaceholder.setPlaceholderType(PlaceholderType.MANU_SEQ);
                paramPlaceholder.setSeq(Integer.parseInt(placeHolder, 10));
            } else paramPlaceholder.setPlaceholderType(PlaceholderType.VAR_NAME);
        }

        eqlRun.setPlaceHolderType(setAndCheckPlaceholderInType(paramPlaceholders, EqlParamPlaceholder.InOut.OUT));
        eqlRun.setPlaceHolderOutType(setAndCheckPlaceholderInType(paramPlaceholders, EqlParamPlaceholder.InOut.IN));
        if (eqlRun.getPlaceHolderType() == PlaceholderType.UNSET)
            eqlRun.setPlaceHolderType(eqlRun.getPlaceHolderOutType());

        eqlRun.setPlaceHolders(paramPlaceholders.toArray(new EqlParamPlaceholder[0]));
    }

    private PlaceholderType setAndCheckPlaceholderInType(List<EqlParamPlaceholder> paramPlaceholders, EqlParamPlaceholder.InOut inOut) {
        PlaceholderType placeHolderInType = PlaceholderType.UNSET;
        for (EqlParamPlaceholder paramPlaceholder : paramPlaceholders)
            if (placeHolderInType != paramPlaceholder.getPlaceholderType()
                    && paramPlaceholder.getInOut() != inOut) {
                if (placeHolderInType != PlaceholderType.UNSET)
                    throw new EqlConfigException("["
                            + (sqlBlock != null ? sqlBlock.getSqlId() : templateSql) + "]中定义的SQL绑定参数设置类型不一致");

                placeHolderInType = paramPlaceholder.getPlaceholderType();
            }

        if (placeHolderInType == PlaceholderType.MANU_SEQ
                && eqlRun.getSqlType() == EqlRun.EqlType.CALL)
            throw new EqlConfigException("["
                    + (sqlBlock != null ? sqlBlock.getSqlId() : templateSql) + "]是存储过程，不支持手工设定参数顺序");

        return placeHolderInType;
    }
}
