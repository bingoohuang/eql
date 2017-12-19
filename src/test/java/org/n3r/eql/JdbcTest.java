package org.n3r.eql;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.DriverManager;

public class JdbcTest {
    @Test @SneakyThrows @Ignore
    public void test1() {
        Class.forName("com.mysql.jdbc.Driver");
        @Cleanup val connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:13306/diamond?useUnicode=true&&characterEncoding=UTF-8" +
                        "&connectTimeout=3000&socketTimeout=3000&autoReconnect=true",
                "diamond", "diamond");
        connection.setAutoCommit(false);
        @Cleanup val ps = connection.prepareStatement("insert into miao values(18600110022, 1, now())");
        int rows = ps.executeUpdate();
        System.out.println(rows);
    }
}
