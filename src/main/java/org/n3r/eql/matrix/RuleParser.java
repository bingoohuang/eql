package org.n3r.eql.matrix;

import com.alibaba.druid.util.StringUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.n3r.eql.matrix.impl.MatrixFunction;
import org.n3r.eql.matrix.impl.MatrixMapper;
import org.n3r.eql.matrix.impl.MatrixRule;
import org.n3r.eql.matrix.impl.MatrixTableField;
import org.n3r.eql.util.EqlUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.n3r.eql.util.EqlUtils.isBlank;
import static org.n3r.eql.util.EqlUtils.trimToEmpty;

public class RuleParser {
    public RulesSet parse(String ruleSpec) {
        Iterable<String> lines = Splitter.on('\n').trimResults().split(ruleSpec);

        RulesSet rulesSet = new RulesSet();

        int lineNo = 0;
        for (String line : lines) {
            ++lineNo;

            if (StringUtils.isEmpty(line)) continue;
            if (line.startsWith("#")) continue; // comments

            try {
                if (line.startsWith("alias")) {
                    parseAlias(rulesSet, line);
                } else if (line.startsWith("rule")) {
                    parseRule(rulesSet, line);
                } else {
                    throw new RuntimeException("unknown format");
                }
            } catch (RuntimeException ex) {
                throw new RuntimeException(ex.getMessage() + " in line " + lineNo + " [" + line + "]");
            }
        }

        return rulesSet;
    }


    static Pattern ruleIndexPattern = Pattern.compile("rule\\s*\\(\\s*(\\d+)\\s*\\)");
    static Pattern javaIdentifierPattern = Pattern.compile("([_\\w][_\\w\\d]*)\\s*\\((.*?)\\)");

    private void parseRule(RulesSet rulesSet, String line) {
        MatrixRule matrixRule = new MatrixRule();

        String remain = trimToEmpty(line);
        remain = parseRuleNo(rulesSet, matrixRule, remain);
        remain = parseFunction(rulesSet, matrixRule, remain);
        parseMapper(rulesSet, matrixRule, remain);

        rulesSet.addRule(matrixRule);
    }

    private void parseMapper(RulesSet rulesSet, MatrixRule matrixRule, String remain) {
        if (StringUtils.isEmpty(remain)) throw new RuntimeException("rule is invalid without mapper");

        Matcher matcher = javaIdentifierPattern.matcher(remain);
        boolean found = matcher.find();

        if (!found || matcher.start() != 0) throw new RuntimeException("mappers invalid ");

        String mapperAlias = matcher.group(1);
        Class<? extends MatrixMapper> mapClass = rulesSet.getMapAlias(mapperAlias);
        if (mapClass == null) throw new RuntimeException("mapper is unknown ");

        MatrixMapper mapper = createMatrixMapper(mapClass);

        String mapperParamsStr = trimToEmpty(matcher.group(2));
        if (isBlank(mapperParamsStr)) throw new RuntimeException("mapper is invalid ");

        List<String> mapperParams = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(mapperParamsStr);
        mapper.config(mapperParams);

        matrixRule.mapper = mapper;
    }

    private String parseFunction(RulesSet rulesSet, MatrixRule matrixRule, String remain) {
        if (StringUtils.isEmpty(remain)) throw new RuntimeException("rule is invalid");

        Matcher matcher = javaIdentifierPattern.matcher(remain);
        boolean found = matcher.find();

        if (!found || matcher.start() != 0) throw new RuntimeException("function is invalid");

        String funcAlias = matcher.group(1);
        Class<? extends MatrixFunction> funcClass = rulesSet.getFunctionAlias(funcAlias);
        if (funcClass == null) throw new RuntimeException("function is unknown");

        String funcParams = trimToEmpty(matcher.group(2));
        if (isBlank(funcParams)) throw new RuntimeException("function should have parameters");

        MatrixFunction func = createMatrixFunction(funcClass);

        List<String> params = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(funcParams);

        List<MatrixTableField> fields = Lists.newArrayList();

        int i = parseFunctionRelativeTableFields(func, params, fields);

        List<String> realFuncParams = Lists.newArrayList();
        for (int ii = params.size(); i < ii; ++i) {
            realFuncParams.add(params.get(i));
        }

        func.configFunctionParameters(realFuncParams.toArray(new String[0]));

        matrixRule.function = func;

        return trimToEmpty(remain.substring(matcher.end()));
    }

    private int parseFunctionRelativeTableFields(MatrixFunction func, List<String> params, List<MatrixTableField> fields) {
        int i = 0;
        for (int ii = params.size(); i < ii; ++i) {
            String param = params.get(i);
            if (!param.startsWith(".")) break;

            int dotPos = param.indexOf('.', 1);
            if (dotPos <= 0 || dotPos == param.length() - 1)
                throw new RuntimeException("rule function should have at least one relative table field");

            String tableName = param.substring(1, dotPos);
            String fieldName = param.substring(dotPos + 1);
            if (!aliasPattern.matcher(tableName).matches())
                throw new RuntimeException("table name is invalid int rule function parameters");

            if (!aliasPattern.matcher(fieldName).matches())
                throw new RuntimeException("field name is invalid int rule function parameters");

            fields.add(new MatrixTableField(tableName, fieldName));
        }

        func.configRelativeTableFields(fields.toArray(new MatrixTableField[0]));
        return i;
    }

    private String parseRuleNo(RulesSet rulesSet, MatrixRule matrixRule, String remain) {
        Matcher matcher = ruleIndexPattern.matcher(remain);
        boolean found = matcher.find();
        if (!found || matcher.start() != 0) throw new RuntimeException("rule is invalid");

        int ruleNo = Integer.parseInt(matcher.group(1));
        MatrixRule rule = rulesSet.getRule(ruleNo);
        if (rule != null) throw new RuntimeException("rule no is duplicated");
        matrixRule.ruleNo = ruleNo;

        // parse function
        return trimToEmpty(remain.substring(matcher.end()));
    }

    private MatrixMapper createMatrixMapper(Class<? extends MatrixMapper> mapClass) {
        try {
            return mapClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private MatrixFunction createMatrixFunction(Class<? extends MatrixFunction> funcClass) {
        try {
            return funcClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Pattern aliasPattern = Pattern.compile("[_\\w]([_\\w\\d])*");

    private void parseAlias(RulesSet rulesSet, String line) {
        String map = line.substring("alias".length());
        String alias = trimToEmpty(map);
        if (StringUtils.isEmpty(alias) || !alias.startsWith("(") || !alias.endsWith(")")) {
            throw new RuntimeException("alias required format: alias(shortName, FQCN)");
        }

        alias = trimToEmpty(alias.substring(1, alias.length() - 1));
        if (StringUtils.isEmpty(alias)) {
            throw new RuntimeException("alias required format: alias(shortName, FQCN)");
        }

        int commaPos = alias.indexOf(',');
        if (commaPos <= 0 || commaPos == alias.length() - 1) {
            throw new RuntimeException("alias required format: alias(shortName, FQCN)");
        }

        String aliasName = trimToEmpty(alias.substring(0, commaPos));
        String fullName = trimToEmpty(alias.substring(commaPos + 1));

        if (!aliasPattern.matcher(aliasName).matches()) {
            throw new RuntimeException("alias short name is invalid");
        }

        Class<? extends MatrixFunction> fullClass = getFullClass(fullName);
        if (fullClass == null) {
            throw new RuntimeException("alias full name is invalid");
        }

        rulesSet.addAlias(aliasName, fullClass);
    }

    @SuppressWarnings("checked")
    private Class<? extends MatrixFunction> getFullClass(String fullName) {
        if (StringUtils.isEmpty(fullName)) return null;

        try {
            return (Class<? extends MatrixFunction>) Class.forName(fullName, false, EqlUtils.getClassLoader());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

}
