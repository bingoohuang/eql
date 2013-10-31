package org.n3r.eql.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.n3r.eql.DbType;
import org.n3r.eql.ex.EqlException;

public class DbTypeFactory {

    public static DbType parseDbType(Connection connection) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String driverName = metaData.getDriverName();

            DbType dbType = new DbType();
            dbType.setDriverName(driverName);

            return dbType;
        }
        catch(SQLException ex) {
            throw new EqlException(ex);
        }

    }

}
