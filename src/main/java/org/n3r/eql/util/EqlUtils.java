package org.n3r.eql.util;

import org.n3r.eql.base.ExpressionEvaluator;
import org.n3r.eql.map.EqlRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

@SuppressWarnings("unchecked")
public class EqlUtils {
    static Logger logger = LoggerFactory.getLogger(EqlUtils.class);

    public static String trimLastUnusedPart(String sql) {
        String returnSql = S.trimRight(sql);
        String upper = S.upperCase(returnSql);
        if (S.endsWith(upper, "WHERE"))
            return returnSql.substring(0, sql.length() - "WHERE".length());

        if (S.endsWith(upper, "AND"))
            return returnSql.substring(0, sql.length() - "AND".length());

        if (S.endsWith(upper, "OR"))
            return returnSql.substring(0, sql.length() - "AND".length());

        return returnSql;
    }

    public static PreparedStatement prepareSql(EqlRun eqlRun, String sqlId) throws SQLException {
        logger.debug("prepare sql for [{}]: {} ", sqlId, eqlRun.getPrintSql());
        return eqlRun.getSqlType().isProcedure()
                ? eqlRun.getConnection().prepareCall(eqlRun.getRunSql())
                : eqlRun.getConnection().prepareStatement(eqlRun.getRunSql());
    }

    public static Collection<?> evalCollection(String collectionExpr, EqlRun eqlRun) {
        ExpressionEvaluator evaluator = eqlRun.getEqlConfig().getExpressionEvaluator();
        Object value = evaluator.eval(collectionExpr, eqlRun);
        if (value instanceof Collection) return (Collection<?>) value;

        throw new RuntimeException(collectionExpr + " in "
                + eqlRun.getParamBean() + " is not an expression of a collection");
    }
}
