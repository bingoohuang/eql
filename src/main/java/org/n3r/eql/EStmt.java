package org.n3r.eql;

import org.n3r.eql.map.EqlRun;
import org.slf4j.Logger;

import java.sql.PreparedStatement;

public interface EStmt {
    void setPreparedStatment(PreparedStatement preparedStatement);

    void setEqlRun(EqlRun subSql);

    void setLogger(Logger logger);

    void setEqlTran(EqlTran eqlTran);

    void closeStmt();

    void params(Object[] params);

    Object[] getParams();

    void setSqlClassPath(String sqlClassPath);
}
