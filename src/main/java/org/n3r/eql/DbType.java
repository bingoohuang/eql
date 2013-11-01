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

    public String getDriverName() {
        return driverName;
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public EqlRun createPageSql(EqlRun eqlRun, EqlPage page) {
        if (EqlUtils.containsIgnoreCase(driverName, "oracle")) {
            return createOraclePageSql(eqlRun, page);
        }

        if (EqlUtils.containsIgnoreCase(driverName, "mysql")) {
            return createMySqlPageSql(eqlRun, page);
        }

        return eqlRun;
    }

    private String tryParseDatabaseId() {
        if (EqlUtils.containsIgnoreCase(driverName, "oracle")) return "oracle";
        if (EqlUtils.containsIgnoreCase(driverName, "mysql")) return "mysql";
        if (EqlUtils.containsIgnoreCase(driverName, "h2")) return "h2";

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
        String pageSql = "SELECT * FROM ( SELECT ROW__.*, ROWNUM RN__ FROM ( " + eqlRun0.getRunSql()
                + " ) ROW__  WHERE ROWNUM <= ?) WHERE RN__ > ?";
        eqlRun.setRunSql(pageSql);

        String printSql = "SELECT * FROM ( SELECT ROW__.*, ROWNUM RN__ FROM ( " + eqlRun0.getPrintSql()
                + " ) ROW__  WHERE ROWNUM <= ?) WHERE RN__ > ?";

        eqlRun.setPrintSql(printSql);
        eqlRun.setExtraBindParams(eqlPage.getStartIndex() + eqlPage.getPageRows(), eqlPage.getStartIndex());

        return eqlRun;
    }
}
