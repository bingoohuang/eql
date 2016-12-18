package org.n3r.eql;

import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;

import java.util.List;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/18.
 */
@EqlerConfig("orcl")
public interface JavaClobDao {
    @Sql("CREATE TABLE IMAGE_BASE64( IMAGE_NAME VARCHAR2(100) PRIMARY KEY, BASE64 CLOB)")
    void createImageBase64();

    @Sql("DROP TABLE IMAGE_BASE64")
    void dropImageBase64();

    @Sql("INSERT INTO IMAGE_BASE64(IMAGE_NAME, BASE64) VALUES(#imageName#, #base64#)")
    void addImageBase64(JavaClobTest.ImageBase64 imageBase64);

    @Sql("SELECT IMAGE_NAME, BASE64 FROM IMAGE_BASE64")
    List<JavaClobTest.ImageBase64> queryAll();

    @Sql("UPDATE IMAGE_BASE64 SET BASE64 = #base64# WHERE IMAGE_NAME = #imageName#")
    int updateImageBase64(JavaClobTest.ImageBase64 imageBase64);
}
