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

    public EqlRun createPageSql(EqlRun subSql, EqlPage page) {
        if (EqlUtils.containsIgnoreCase(driverName, "oracle"))
            return createOraclePageSql(subSql, page);

        if (EqlUtils.containsIgnoreCase(driverName, "mysql"))
            return createMySqlPageSql(subSql, page);

        return subSql;
    }

    private EqlRun createMySqlPageSql(EqlRun subSql, EqlPage page) {
        EqlRun pageSubSql = subSql.clone();
        String pageSql = subSql.getSql() + " LIMIT ?,?";
        pageSubSql.setSql(pageSql);
        pageSubSql.setExtraBindParams(page.getStartIndex(), page.getPageRows());

        return pageSubSql;
    }

    private EqlRun createOraclePageSql(EqlRun subSql, EqlPage eqlPage) {
        EqlRun eqlRun = subSql.clone();
        String pageSql = "SELECT * FROM ( SELECT ROW__.*, ROWNUM RN__ FROM ( " + subSql.getSql()
                + " ) ROW__  WHERE ROWNUM <= ?) WHERE RN__ > ?";
        eqlRun.setSql(pageSql);
        eqlRun.setExtraBindParams(eqlPage.getStartIndex() + eqlPage.getPageRows(), eqlPage.getStartIndex());

        return eqlRun;
    }
}
