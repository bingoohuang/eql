package org.n3r.eql.parser;

import com.google.common.base.Objects;
import org.n3r.eql.base.ExpressionEvaluator;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.map.EqlDynamic;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.Names;

import java.util.List;

public class DynamicReplacer {
    private Object[] dynamics;
    private EqlRun eqlRun;

    public void replaceDynamics(EqlRun eqlRun) {
        this.eqlRun = eqlRun;
        this.dynamics = eqlRun.getDynamics();
        if (dynamics != null && dynamics.length > 0 && eqlRun.getEqlDynamic() == null) {
            eqlRun.setEqlDynamic(new DynamicParser().parseRawSql(eqlRun.getRunSql()));
            eqlRun.setEvalEqlDynamic(new DynamicParser().parseRawSql(eqlRun.getEvalSql()));
        }

        EqlDynamic eqlDynamic = eqlRun.getEqlDynamic();
        if (eqlDynamic == null) return;

        eqlRun.setRunSql(replaceRunSqlDynamics(eqlDynamic));
        eqlRun.setEvalSql(replaceRunSqlDynamics(eqlRun.getEvalEqlDynamic()));
    }

    private String replaceRunSqlDynamics(EqlDynamic eqlDynamic) {
        List<String> sqlPieces = eqlDynamic.getSqlPieces();
        StringBuilder runSql = new StringBuilder(sqlPieces.get(0));
        int ii = sqlPieces.size();

        switch (eqlDynamic.getPlaceholdertype()) {
            case AUTO_SEQ:
                for (int i = 1; i < ii; ++i)
                    runSql.append(findDynamicByIdx(i - 1)).append(sqlPieces.get(i));
                break;
            case MANU_SEQ:
                for (int i = 1; i < ii; ++i)
                    runSql.append(findDynamicBySeq(eqlDynamic, i - 1)).append(sqlPieces.get(i));
                break;
            case VAR_NAME:
                for (int i = 1; i < ii; ++i)
                    runSql.append(findDynamicByName(eqlDynamic, i - 1)).append(sqlPieces.get(i));
                break;
            default:
                break;
        }

        return runSql.toString();
    }

    private Object findDynamicByIdx(int index) {
        if (index < dynamics.length) return dynamics[index];

        throw new EqlExecuteException("[" + eqlRun.getSqlId() + "] lack dynamic params");
    }

    private Object findDynamicBySeq(EqlDynamic eqlDynamic, int index) {
        return findDynamicByIdx(eqlDynamic.getPlaceholders()[index].getSeq() - 1);
    }

    private Object findDynamicByName(EqlDynamic eqlDynamic, int index) {
        String varName = eqlDynamic.getPlaceholders()[index].getPlaceholder();

        ExpressionEvaluator evaluator = eqlRun.getEqlConfig().getExpressionEvaluator();
        Object property = evaluator.evalDynamic(varName, eqlRun);
        if (property != null) return property;

        String propertyName = Names.underscoreNameToPropertyName(varName);
        if (!Objects.equal(propertyName, varName))
            property = evaluator.evalDynamic(propertyName, eqlRun);

        return property;
    }
}
