package org.n3r.eql.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.map.EqlRun;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;

@SuppressWarnings("unchecked")
public class EqlUtils {
    public static final String USER_HOME = System.getProperty("user.home");
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static String expandUserHome(String path) {
        if (path.startsWith("~")) {
            return USER_HOME + path.substring(1);
        }

        return path;
    }

    public static void compatibleWithUserToUsername(Map<String, String> params) {
        if (params.containsKey("username")) return;
        if (params.containsKey("user"))
            params.put("username", params.get("user"));
    }

    @SneakyThrows
    public static String getDriverNameFromConnection(DataSource dataSource) {
        @Cleanup val connection = dataSource.getConnection();
        return connection.getMetaData().getDriverName();
    }

    @SneakyThrows
    public static String getJdbcUrlFromConnection(DataSource dataSource) {
        @Cleanup val connection = dataSource.getConnection();
        return connection.getMetaData().getURL();
    }

    public static Map<String, Object> newExecContext(Object[] originParams, Object[] dynamics) {
        val execContext = Maps.<String, Object>newHashMap();
        execContext.put("_time", new Timestamp(System.currentTimeMillis()));
        execContext.put("_date", new java.util.Date());
        execContext.put("_host", HostAddress.getHost());
        execContext.put("_ip", HostAddress.getIp());
        execContext.put("_results", newArrayList());
        execContext.put("_lastResult", "");

        Object[] params = convertParams(originParams);

        execContext.put("_params", params);
        if (params != null) {
            execContext.put("_paramsCount", params.length);
            for (int i = 0; i < params.length; ++i)
                execContext.put("_" + (i + 1), params[i]);
        }

        execContext.put("_dynamics", dynamics);
        if (dynamics != null)
            execContext.put("_dynamicsCount", dynamics.length);

        return execContext;
    }

    private static Object[] convertParams(Object[] originParams) {
        if (originParams == null) return null;

        Object[] objects = new Object[originParams.length];
        for (int i = 0; i < originParams.length; ++i) {
            objects[i] = convertParam(originParams[i]);
        }

        return objects;
    }

    private static Object convertParam(Object originParam) {
        if (originParam instanceof List) return originParam;
        if (originParam instanceof Iterable) {
            return Lists.newArrayList((Iterable) originParam);
        }
        return originParam;
    }

    static Pattern endWithWhere = Pattern.compile("\\bWHERE$");
    static Pattern endWithAnd = Pattern.compile("\\bAND$");
    static Pattern endWithOr = Pattern.compile("\\bOR$");

    public static String trimLastUnusedPart(String sql) {
        val returnSql = S.trimRight(sql);
        val upper = S.upperCase(returnSql);
        if (endWithWhere.matcher(upper).find())
            return S.trimRight(returnSql.substring(0, returnSql.length() - "WHERE".length()));

        if (endWithAnd.matcher(upper).find())
            return S.trimRight(returnSql.substring(0, returnSql.length() - "AND".length()));

        if (endWithOr.matcher(upper).find())
            return S.trimRight(returnSql.substring(0, returnSql.length() - "OR".length()));

        return returnSql;
    }

    @SneakyThrows
    public static PreparedStatement prepareSQL(
            String sqlClassPath, EqlConfig eqlConfig, EqlRun eqlRun, String sqlId, String tagSqlId) {
        val log = Logs.createLogger(eqlConfig, sqlClassPath, sqlId, tagSqlId, "prepare");

        log.debug(eqlRun.getPrintSql());

        val conn = eqlRun.getConnection();
        val sql = eqlRun.getRunSql();
        val procedure = eqlRun.getSqlType().isProcedure();
        val ps = procedure ? conn.prepareCall(sql) : conn.prepareStatement(sql);

        setQueryTimeout(eqlConfig, ps);

        return ps;
    }

    public static int getConfigInt(EqlConfig eqlConfig, String key, int defaultValue) {
        val configValue = eqlConfig.getStr(key);
        if (S.isBlank(configValue)) return defaultValue;

        if (configValue.matches("\\d+")) return Integer.parseInt(configValue);
        return defaultValue;
    }

    @SneakyThrows
    public static void setQueryTimeout(EqlConfig eqlConfig, Statement stmt) {
        int queryTimeout = getConfigInt(eqlConfig, "query.timeout.seconds", 60);
        if (queryTimeout <= 0) queryTimeout = 60;

        try {
            stmt.setQueryTimeout(queryTimeout);
        } catch (Exception ignore) {

        }
    }

    public static Iterable<?> evalCollection(String collectionExpr, EqlRun eqlRun) {
        val evaluator = eqlRun.getEqlConfig().getExpressionEvaluator();
        val value = evaluator.eval(collectionExpr, eqlRun);
        if (value == null) return null;

        if (value instanceof Iterable) return (Iterable<?>) value;
        if (value.getClass().isArray()) return newArrayList((Object[]) value);
        if (value instanceof Map) return ((Map) value).entrySet();

        throw new RuntimeException(collectionExpr + " in "
                + eqlRun.getParamBean() + " is not an expression of a collection");
    }

    public static String collectionExprString(String collectionExpr, EqlRun eqlRun) {
        val evaluator = eqlRun.getEqlConfig().getExpressionEvaluator();
        val value = evaluator.eval(collectionExpr, eqlRun);
        if (value == null) return null;

        if (value instanceof Iterable || value.getClass().isArray()) return collectionExpr;
        if (value instanceof Map) return collectionExpr + ".entrySet().toArray()";

        throw new RuntimeException(collectionExpr + " in "
                + eqlRun.getParamBean() + " is not an expression of a collection");
    }

    /*
     * Determine if SQLException#getSQLState() of the catched SQLException
     * starts with 23 which is a constraint violation as per the SQL specification.
     * It can namely be caused by more factors than "just" a constraint violation.
     * You should not amend every SQLException as a constraint violation.
     * ORACLE:
     * [2017-03-26 15:13:07] [23000][1] ORA-00001: 违反唯一约束条件 (SYSTEM.SYS_C007109)
     * MySQL:
     * [2017-03-26 15:17:27] [23000][1062] Duplicate entry '1' for key 'PRIMARY'
     * H2:
     * [2017-03-26 15:19:52] [23505][23505] Unique index or primary key violation:
     * "PRIMARY KEY ON PUBLIC.TT(A)"; SQL statement:
     *
     */
    public static boolean isConstraintViolation(Exception e) {
        return e instanceof SQLException
                && ((SQLException) e).getSQLState().startsWith("23");
    }
}
