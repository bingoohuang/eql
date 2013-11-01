package org.n3r.eql;

import org.n3r.eql.ex.EqlException;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.EqlUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class DbType {
    private String driverName;
    private String databaseId;

    public static DbType parseDbType(Connection connection) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String driverName = metaData.getDriverName();

            return new DbType(driverName);
        } catch (SQLException ex) {
            throw new EqlException(ex);
        }
    }

    public DbType(String driverName) {
        this.driverName = driverName;
        databaseId = tryParseDatabaseId();
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public EqlRun createPageSql(EqlRun eqlRun, EqlPage page) {
        if ("oracle".equals(databaseId)) return createOraclePageSql(eqlRun, page);
        if ("mysql".equals(databaseId)) return createMySqlPageSql(eqlRun, page);
        if ("h2".equals(databaseId)) return createH2PageSql(eqlRun, page);

        return eqlRun;
    }


    private String tryParseDatabaseId() {
        if (EqlUtils.containsIgnoreCase(driverName, "oracle")) return "oracle";
        if (EqlUtils.containsIgnoreCase(driverName, "mysql")) return "mysql";
        if (EqlUtils.containsIgnoreCase(driverName, "h2")) return "h2";
        if (EqlUtils.containsIgnoreCase(driverName, "db2")) return "db2";

        return driverName;
    }

    private EqlRun createMySqlPageSql(EqlRun eqlRun, EqlPage page) {
        EqlRun eqlRun1 = eqlRun.clone();
        eqlRun1.setRunSql(eqlRun.getRunSql() + " LIMIT ?,?");
        eqlRun1.setPrintSql(eqlRun.getPrintSql() + " LIMIT ?,?");
        eqlRun1.setExtraBindParams(page.getStartIndex(), page.getPageRows());

        return eqlRun1;
    }

    private EqlRun createOraclePageSql(EqlRun eqlRun0, EqlPage eqlPage) {
        EqlRun eqlRun = eqlRun0.clone();
        eqlRun.setRunSql(createOraclePageSql(eqlRun.getRunSql()));
        eqlRun.setPrintSql(createOraclePageSql(eqlRun.getPrintSql()));

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
        eqlRun.setPrintSql(createH2PageSql(eqlRun.getPrintSql()));

        int endIndex = eqlPage.getStartIndex() + eqlPage.getPageRows();
        eqlRun.setExtraBindParams(endIndex, eqlPage.getStartIndex());

        return eqlRun;
    }


    private String createH2PageSql(String sql) {
        return "SELECT * FROM ( SELECT *, ROWNUM() RN__ FROM (" + sql
                + " ) ROW__  WHERE ROWNUM() <= ?) WHERE RN__ > ?";
    }

    public EqlRun createTotalSql(EqlRun currRun) {
        EqlRun totalEqlSql = currRun.clone();

        totalEqlSql.setRunSql(createTotalSql(totalEqlSql.getRunSql()));
        totalEqlSql.setPrintSql(createTotalSql(totalEqlSql.getPrintSql()));

        totalEqlSql.setWillReturnOnlyOneRow(true);

        return totalEqlSql;
    }

    private  String createTotalSql(String runSql) {
        String sql = runSql.toUpperCase();

        boolean oneFromWoDistinctOrGroupby = false;

        // find the first position of FROM
        int fromPos = sql.indexOf("FROM");

        // find the position of DISTINCT
        if (sql.indexOf("DISTINCT") < 0) { // without DISTINCT
            if (fromPos >= 0 && sql.indexOf("FROM", fromPos + 4) < 0) { // only one from
                if (sql.indexOf("GROUP") < 0) oneFromWoDistinctOrGroupby = true; // without GROUP BY
            }
        }

        if (fromPos < 0) fromPos = 0;

        return oneFromWoDistinctOrGroupby
                ? "SELECT COUNT(*) AS CNT " + runSql.substring(fromPos)
                : ("SELECT COUNT(*) CNT__ FROM (" + runSql + ")" + ("mysql".equals(databaseId) ? " total" : ""));
    }
}
