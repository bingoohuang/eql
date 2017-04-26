package org.n3r.eql;

import org.n3r.eql.ex.EqlException;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.S;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class DbDialect {
    private final String jdbcUrl;
    private String driverName;
    private String databaseId;

    public static DbDialect parseDbType(Connection connection) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String driverName = metaData.getDriverName();
            String jdbcUrl = metaData.getURL();

            return new DbDialect(driverName, jdbcUrl);
        } catch (SQLException ex) {
            throw new EqlException(ex);
        }
    }

    public static DbDialect parseDbType(String driverName, String jdbcUrl) {
        return new DbDialect(driverName, jdbcUrl);
    }

    public DbDialect(String driverName, String jdbcUrl) {
        this.driverName = driverName;
        this.jdbcUrl = jdbcUrl;
        databaseId = tryParseDatabaseId();
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public EqlRun createPageSql(EqlRun eqlRun, EqlPage page) {
        if ("oracle".equals(databaseId)) return createOraclePageSql(eqlRun, page);
        if ("mysql".equals(databaseId)) return createMySqlPageSql(eqlRun, page);
        if ("h2".equals(databaseId)) return createH2PageSql(eqlRun, page);

        if ("sqlserver".equals(databaseId)) return createSqlserverPageSql(eqlRun, page);
        return eqlRun;
    }


    private String tryParseDatabaseId() {
        String driverOrUrl = driverName;
        if (driverOrUrl == null) driverOrUrl = jdbcUrl;
        if (S.containsIgnoreCase(driverOrUrl, "oracle")) return "oracle";
        if (S.containsIgnoreCase(driverOrUrl, "mysql")) return "mysql";
        if (S.containsIgnoreCase(driverOrUrl, "h2")) return "h2";
        if (S.containsIgnoreCase(driverOrUrl, "db2")) return "db2";
        if (S.containsIgnoreCase(driverOrUrl, "sqlserver")) return "sqlserver";

        return driverName;
    }

    private EqlRun createMySqlPageSql(EqlRun eqlRun, EqlPage page) {
        EqlRun eqlRun1 = eqlRun.clone();
        eqlRun1.setRunSql(eqlRun.getRunSql() + " LIMIT ?,?");
        eqlRun1.setExtraBindParams(page.getStartIndex(), page.getPageRows());

        return eqlRun1;
    }

    private EqlRun createOraclePageSql(EqlRun eqlRun0, EqlPage eqlPage) {
        EqlRun eqlRun = eqlRun0.clone();
        eqlRun.setRunSql(createOraclePageSql(eqlRun.getRunSql()));

        int endIndex = eqlPage.getStartIndex() + eqlPage.getPageRows();
        eqlRun.setExtraBindParams(endIndex, eqlPage.getStartIndex());

        return eqlRun;
    }

    private String createOraclePageSql(String sql) {
        return "SELECT * FROM ( SELECT ROW__.*, ROWNUM RN__ FROM ( " + sql
                + " ) ROW__  WHERE ROWNUM <= ?) WHERE RN__ > ?";
    }

    private EqlRun createH2PageSql(EqlRun eqlRun0, EqlPage eqlPage) {
        EqlRun eqlRun = eqlRun0.clone();
        eqlRun.setRunSql(createH2PageSql(eqlRun.getRunSql()));

        int endIndex = eqlPage.getStartIndex() + eqlPage.getPageRows();
        eqlRun.setExtraBindParams(endIndex, eqlPage.getStartIndex());

        return eqlRun;
    }


    private String createH2PageSql(String sql) {
        return "SELECT * FROM ( SELECT *, ROWNUM() RN__ FROM (" + sql
                + " ) ROW__  WHERE ROWNUM() <= ?) WHERE RN__ > ?";
    }

    private EqlRun createSqlserverPageSql(EqlRun eqlRun0, EqlPage eqlPage) {
        EqlRun eqlRun = eqlRun0.clone();
        eqlRun.setRunSql(createSqlserverPageSql(eqlRun.getRunSql()));

        int endIndex = eqlPage.getStartIndex() + eqlPage.getPageRows();
        eqlRun.setExtraBindParams(endIndex, eqlPage.getStartIndex());

        return eqlRun;
    }

    private String createSqlserverPageSql(String sql) {
        return "SELECT * FROM (SELECT ROW_NUMBER () OVER (ORDER BY getdate() ASC) RowNumber ,* FROM  "
                + "  (" + sql +")T_   )TT_ "
                + "  WHERE    TT_.RowNumber <=?   and  TT_.RowNumber>?";
    }

    public EqlRun createTotalSql(EqlRun currRun) {
        EqlRun totalEqlRun = currRun.clone();

        totalEqlRun.setRunSql(createTotalSql(totalEqlRun.getRunSql()));

        totalEqlRun.setWillReturnOnlyOneRow(true);

        return totalEqlRun;
    }

    static Pattern orderByPattern = Pattern.compile("\\border\\s+by\\b");

    private String createTotalSql(String runSql) {
        String sql = runSql.toUpperCase();

        boolean oneFromWoDistinctOrGroupby = false;

        // find the first position of FROM
        int fromPos = sql.indexOf("FROM");

        // find the position of DISTINCT
        if (sql.indexOf("DISTINCT") < 0) { // without DISTINCT
            if (fromPos >= 0 && sql.indexOf("FROM", fromPos + 4) < 0) { // only one from
                if (sql.indexOf("GROUP") < 0 && !orderByPattern.matcher(sql).find())
                    oneFromWoDistinctOrGroupby = true; // without GROUP BY
            }
        }

        if (fromPos < 0) fromPos = 0;

        // if bound parameter if found before 'from', we can not remove the select part.
        if (oneFromWoDistinctOrGroupby) {
            int paramBoundPos = sql.indexOf('?');
            if (paramBoundPos >= 0 && paramBoundPos < fromPos) {
                oneFromWoDistinctOrGroupby = false;
            }
        }

        return oneFromWoDistinctOrGroupby
                ? "SELECT COUNT(*) AS CNT " + runSql.substring(fromPos)
                : "SELECT COUNT(*) CNT__ FROM (" + runSql + ") TOTAL";
    }
}
