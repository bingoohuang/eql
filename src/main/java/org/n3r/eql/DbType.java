package org.n3r.eql;

import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.EqlUtils;

public class DbType {
    private String driverName;

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public EqlRun createPageSql(EqlRun eqlRun, EqlPage page) {
        if (EqlUtils.containsIgnoreCase(driverName, "oracle"))
            return createOraclePageSql(eqlRun, page);

        if (EqlUtils.containsIgnoreCase(driverName, "mysql"))
            return createMySqlPageSql(eqlRun, page);

        return eqlRun;
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
