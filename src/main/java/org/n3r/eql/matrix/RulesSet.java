package org.n3r.eql.matrix;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.n3r.eql.matrix.func.ModFunction;
import org.n3r.eql.matrix.func.PrefixFunction;
import org.n3r.eql.matrix.func.RangeFunction;
import org.n3r.eql.matrix.func.ValFunction;
import org.n3r.eql.matrix.impl.GotoRealPartition;
import org.n3r.eql.matrix.impl.MatrixFunction;
import org.n3r.eql.matrix.impl.MatrixMapper;
import org.n3r.eql.matrix.impl.MatrixRule;
import org.n3r.eql.matrix.mapper.DefaultMatrixMapper;

import java.util.List;
import java.util.Map;

public class RulesSet {
    private Map<String, Class<? extends MatrixFunction>> funcAlias = Maps.newHashMap();
    private Map<String, Class<? extends MatrixMapper>> mapperAlias = Maps.newHashMap();
    private Map<Integer, MatrixRule> rulesMap = Maps.newHashMap();
    private List<MatrixRule> rules = Lists.newArrayList();

    {
        funcAlias.put("pre", PrefixFunction.class);
        funcAlias.put("mod", ModFunction.class);
        funcAlias.put("val", ValFunction.class);
        funcAlias.put("range", RangeFunction.class);

        mapperAlias.put("map", DefaultMatrixMapper.class);
    }

    public Class<? extends MatrixFunction> getFunctionAlias(String aliasName) {
        return funcAlias.get(aliasName);
    }

    public Class<? extends MatrixMapper> getMapAlias(String mapperAliasName) {
        return mapperAlias.get(mapperAliasName);
    }

    public void addAlias(String aliasName, Class<? extends MatrixFunction> fullClass) {
        funcAlias.put(aliasName, fullClass);
    }

    public void addRule(MatrixRule matrixRule) {
        rules.add(matrixRule);
        rulesMap.put(matrixRule.ruleNo, matrixRule);
    }

    public RealPartition find(MatrixTableFieldValue... fieldValues) {
        for (MatrixRule rule : rules) {
            RealPartition realPartition = rule.go(fieldValues);
            if (realPartition == null) continue;

            if (realPartition instanceof GotoRealPartition) {
                GotoRealPartition gotoRealPartition = (GotoRealPartition) realPartition;
                int gotoRuleNum = gotoRealPartition.getGotoRuleNum();
                MatrixRule gotoRule = rulesMap.get(gotoRuleNum);
                if (gotoRule != null) return gotoRule.go(fieldValues);

                throw new RuntimeException("rule " + gotoRuleNum + " is undefinned");
            }

            return realPartition;
        }

        return null;
    }

    public MatrixRule getRule(int ruleNo) {
        return rulesMap.get(ruleNo);
    }

    public boolean relativeTo(String tableName) {
        for (MatrixRule rule : rules) {
            if (rule.relativeTo(tableName)) return true;
        }
        return false;
    }

    public boolean relativeTo(String tableName, String columnName) {
        for (MatrixRule rule : rules) {
            if (rule.relativeTo(tableName, columnName)) return true;
        }
        return false;
    }
}
