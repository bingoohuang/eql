package org.n3r.eql;

import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcTest {
    @Test
    //@Ignore
    public void test1() throws ClassNotFoundException, SQLException {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection connection = DriverManager.getConnection(
                "jdbc:oracle:thin:@127.0.0.1:1521:orcl", "orcl", "orcl");
        connection.setAutoCommit(false);
        PreparedStatement ps = connection.prepareStatement(
                "update ESQL_TEST set B = 'BBB'");
        boolean execute = ps.execute();
        System.out.println(execute);
        ps.close();
        connection.close();
    }
}
