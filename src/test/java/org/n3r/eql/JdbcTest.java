package org.n3r.eql;

import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcTest {
    @Test
    @Ignore
    public void test1() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/diamond?useUnicode=true&&characterEncoding=UTF-8" +
                        "&connectTimeout=3000&socketTimeout=3000&autoReconnect=true",
                "diamond", "diamond");
        connection.setAutoCommit(false);
        PreparedStatement ps = connection.prepareStatement(
                "insert into miao values(18600110022, 1, now())");
        int rows = ps.executeUpdate();
        System.out.println(rows);
        ps.close();
        connection.close();
    }
}
