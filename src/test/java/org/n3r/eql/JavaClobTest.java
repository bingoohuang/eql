package org.n3r.eql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.eqler.EqlerFactory;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.truth.Truth.assertThat;

/*
http://stackoverflow.com/questions/19486648/how-to-retrive-the-clob-value-from-oracle-using-java

Clob clob = resultSet.getClob("DETAILED_DESCRIPTION")
record.add(clob.getSubString(1, (int) clob.length());

http://stackoverflow.com/questions/5067485/create-clob-from-long-string-for-jdbc

if(obj instanceof String && ((String) obj).length() >= 4000) {
    Clob clob = connection.createClob();
    clob.setString(1, (String) obj);
    stmt.setClob(i+1, clob);
}
 */

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/18.
 */
public class JavaClobTest {
    @EqlerConfig("orcl")
    public interface JavaClobDao {
        @Sql("CREATE TABLE IMAGE_BASE64( IMAGE_NAME VARCHAR2(100) PRIMARY KEY, BASE64 CLOB)")
        void createImageBase64();

        @Sql("DROP TABLE IMAGE_BASE64")
        void dropImageBase64();

        @Sql("INSERT INTO IMAGE_BASE64(IMAGE_NAME, BASE64) VALUES(#imageName#, #base64#)")
        void addImageBase64(JavaClobTest.ImageBase64 imageBase64);

        @Sql("SELECT IMAGE_NAME, BASE64 FROM IMAGE_BASE64 WHERE IMAGE_NAME = ##")
        JavaClobTest.ImageBase64 queryImageBase64(String imageName);

        @Sql("UPDATE IMAGE_BASE64 SET BASE64 = #base64# WHERE IMAGE_NAME = #imageName#")
        int updateImageBase64(JavaClobTest.ImageBase64 imageBase64);
    }

    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger("eql.org.n3r.eql.JavaClobTest$JavaClobDaoImpl");
        log.debug("something");
    }

    static JavaClobDao javaClobDao = EqlerFactory.getEqler(JavaClobDao.class);

    @BeforeClass
    public static void beforeClass() {
        javaClobDao.createImageBase64();
    }

    @AfterClass
    public static void afterClass() {
        javaClobDao.dropImageBase64();
    }

    @Test
    public void test() {
        ImageBase64 imageBase64 = new ImageBase64("TEST", "BLABLAKKK");
        javaClobDao.addImageBase64(imageBase64);

        val imageBase64s = javaClobDao.queryImageBase64("TEST");
        assertThat(imageBase64s).isEqualTo(imageBase64);

        imageBase64.setBase64("中华人民共和国");
        int rows = javaClobDao.updateImageBase64(imageBase64);
        assertThat(rows).isEqualTo(1);

        val newImageBase64s = javaClobDao.queryImageBase64("TEST");
        assertThat(newImageBase64s).isEqualTo(imageBase64);
    }

    @Test
    public void testBig() {
        String base64 = RandomStringUtils.randomAlphanumeric(10000);
        ImageBase64 imageBase64 = new ImageBase64("TESTBIG", base64);
        javaClobDao.addImageBase64(imageBase64);

        val imageBase64s = javaClobDao.queryImageBase64("TESTBIG");
        assertThat(imageBase64s).isEqualTo(imageBase64);

        base64 = RandomStringUtils.randomAlphanumeric(10000);
        imageBase64.setBase64(base64);
        int rows = javaClobDao.updateImageBase64(imageBase64);
        assertThat(rows).isEqualTo(1);

        val newImageBase64s = javaClobDao.queryImageBase64("TESTBIG");
        assertThat(newImageBase64s).isEqualTo(imageBase64);
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class ImageBase64 {
        private String imageName;
        private String base64;
    }
}
