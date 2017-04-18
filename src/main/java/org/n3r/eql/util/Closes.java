package org.n3r.eql.util;

import org.n3r.eql.EqlTran;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Closes {

    public static void closeQuietly(Statement stmt) {
        if (stmt == null) return;

        try {
            stmt.close();
        } catch (SQLException e) {
            // Ignore
        }
    }

    public static void closeQuietly(ResultSet rs, Statement ps) {
        closeQuietly(rs);
        closeQuietly(ps);

    }

    public static void closeQuietly(ResultSet rs) {
        if (rs == null) return;

        try {
            rs.close();
        } catch (SQLException e) {
            // Ignore
        }
    }

    public static void closeQuietly(EqlTran eqlTran) {
        if (eqlTran == null) return;

        try {
            eqlTran.close();
        } catch (Exception e) {
            // Ignore
        }
    }

    public static void closeQuietly(Closeable writer) {
        if (writer == null) return;

        try {
            writer.close();
        } catch (IOException e) {
            // Ignore
        }

    }


    public static void closeQuietly(Statement cs, Connection connection) {
        closeQuietly(cs);
        closeQuietly(connection);
    }

    public static void closeQuietly(Connection connection) {
        if (connection == null) return;

        try {
            connection.close();
        } catch (SQLException e) {
            // Ignore
        }
    }
}
