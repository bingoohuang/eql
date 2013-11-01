package org.n3r.eql.map;

import java.sql.CallableStatement;
import java.sql.SQLException;

public interface EqlCallableReturnMapper {
    Object mapResult(EqlRun eqlRun, CallableStatement cs) throws SQLException;
}
