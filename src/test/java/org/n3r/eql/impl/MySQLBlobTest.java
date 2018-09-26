package org.n3r.eql.impl;

import com.github.bingoohuang.westid.WestId;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.n3r.eql.Eql;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Statement;

import static com.google.common.truth.Truth.assertThat;

public class MySQLBlobTest {
    @SneakyThrows
    private static void copyStream(InputStream is, OutputStream output) {
        byte[] buffer = new byte[1024];
        for (; ; ) {
            int n = is.read(buffer);
            if (n < 0) break;
            output.write(buffer, 0, n);
        }
    }

    @Test @SneakyThrows
    public void demoBlob() {
//        String url = "jdbc:mysql://192.168.99.100:13306/dba?useUnicode=true&&characterEncoding=UTF-8&connectTimeout=3000&socketTimeout=3000&autoReconnect=true";
//        String username = "root";
//        String pwd = "my-secret-pw";
//        @Cleanup val conn = DriverManager.getConnection(url, username, pwd);

        @Cleanup val conn = new Eql("mysql").getConnection();
        @Cleanup Statement statement = conn.createStatement();
        statement.execute("drop table if exists last_excel");
        statement.execute("create table last_excel (id varchar(30), excel longblob)");

        val sql = "insert into last_excel(id, excel) values(?, ?)";
        @Cleanup val ps = conn.prepareStatement(sql);

        val classLoader = MySQLBlobTest.class.getClassLoader();
        @Cleanup val is = classLoader.getResourceAsStream("blob.xlsx");
        assertThat(is).isNotNull();

        val id = String.valueOf(WestId.next());
        ps.setString(1, id);
        ps.setBinaryStream(2, is);
        ps.executeUpdate();

        val sql2 = "select excel from last_excel where id = ?";
        @Cleanup val ps2 = conn.prepareStatement(sql2);
        ps2.setString(1, id);
        @Cleanup val rs = ps2.executeQuery();

        assertThat(rs.next()).isTrue();
        @Cleanup InputStream input = rs.getBinaryStream(1);

        @Cleanup val is2 = classLoader.getResourceAsStream("blob.xlsx");
        assertThat(IOUtils.contentEquals(is2, input)).isTrue();
//      File file = new File(id + ".xlsx");
//      @Cleanup val output = new FileOutputStream(file);
//      copyStream(input, output);
//      System.out.println(file);
    }
}
