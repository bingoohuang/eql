package org.n3r.eql.parser;

import com.google.common.base.Objects;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.map.EqlDynamic;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.EqlUtils;

import java.util.List;

public class DynamicReplacer {
    public String repaceDynamics(EqlRun eqlRun, Object[] dynamics) {
        if (dynamics != null && dynamics.length > 0 && eqlRun.getEqlDynamic() == null)
            eqlRun.setEqlDynamic(new DynamicParser().parseRawSql(eqlRun.getSql()));

        EqlDynamic eqlDynamic = eqlRun.getEqlDynamic();
        if (eqlDynamic == null) return eqlRun.getSql();

        List<String> sqlPieces = eqlDynamic.getSqlPieces();
        StringBuilder realSql = new StringBuilder(sqlPieces.get(0));

        switch (eqlDynamic.getPlaceholdertype()) {
            case AUTO_SEQ:
                for (int i = 1, ii = sqlPieces.size(); i < ii; ++i)
                    realSql.append(getDynamicByIndex(dynamics, i - 1, eqlRun.getSqlId())).append(sqlPieces.get(i));
                break;
            case MANU_SEQ:
                for (int i = 1, ii = sqlPieces.size(); i < ii; ++i)
                    realSql.append(findDynamicBySeq(dynamics, eqlDynamic, i - 1, eqlRun.getSqlId())).append(
                            sqlPieces.get(i));
                break;
            case VAR_NAME:
                for (int i = 1, ii = sqlPieces.size(); i < ii; ++i)
                    realSql.append(findDynamicByName(dynamics, eqlDynamic, i - 1)).append(sqlPieces.get(i));
                break;
            default:
                break;
        }

        return realSql.toString();
    }

    private Object getDynamicByIndex(Object[] dynamics, int index, String sqlId) {
        if (index < dynamics.length)
            return dynamics[index];

        throw new EqlExecuteException("[" + sqlId + "]执行过程中缺少动态替换参数");
    }

    private Object findDynamicBySeq(Object[] dynamics, EqlDynamic eqlDynamic, int index, String sqlId) {
        return getDynamicByIndex(dynamics, eqlDynamic.getPlaceholders()[index].getSeq() - 1, sqlId);
    }

    private Object findDynamicByName(Object[] dynamics, EqlDynamic eqlDynamic, int index) {
        Object bean = dynamics[0];

        String varName = eqlDynamic.getPlaceholders()[index].getPlaceholder();
        Object property = EqlUtils.getPropertyQuietly(bean, varName);
        if (property == null) {
            String propertyName = EqlUtils.convertUnderscoreNameToPropertyName(varName);
            if (!Objects.equal(propertyName, varName))
                property = EqlUtils.getPropertyQuietly(bean, propertyName);
        }
        return property;
    }
}
