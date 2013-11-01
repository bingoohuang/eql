package org.n3r.eql;

import com.google.common.base.Splitter;
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
        }
        catch(SQLException ex) {
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
        Iterable<String> split = Splitter.on('.').split(driverName);
        for (String part : split) {
            if (part.equalsIgnoreCase("com")) continue;
            if (part.equalsIgnoreCase("org")) continue;
            if (part.equalsIgnoreCase("jdbc")) continue;
            if (part.equalsIgnoreCase("driver")) continue;

            return part;
        }

        return driverName;
    }

    private EqlRun createMySqlPageSql(EqlRun eqlRun, EqlPage page) {
        EqlRun eqlRun1 = eqlRun.clone();
        String pageSql = eqlRun.getRunSql() + " LIMIT ?,?";
        eqlRun1.setRunSql(pageSql);
        eqlRun1.setExtraBindParams(page.getStartIndex(), page.getPageRows());

        return eqlRun1;
    }

    private EqlRun createOraclePageSql(EqlRun subSql, EqlPage eqlPage) {
        EqlRun eqlRun = subSql.clone();
        String pageSql = "SELECT * FROM ( SELECT ROW__.*, ROWNUM RN__ FROM ( " + subSql.getRunSql()
                + " ) ROW__  WHERE ROWNUM <= ?) WHERE RN__ > ?";
        eqlRun.setRunSql(pageSql);
        eqlRun.setExtraBindParams(eqlPage.getStartIndex() + eqlPage.getPageRows(), eqlPage.getStartIndex());

        return eqlRun;
    }
}
