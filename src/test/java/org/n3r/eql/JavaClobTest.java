package org.n3r.eql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.eqler.EqlerFactory;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/18.
 */
public class JavaClobTest {
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

        val imageBase64s = javaClobDao.queryAll();
        assertThat(imageBase64s).containsExactly(imageBase64);

        imageBase64.setBase64("中华人民共和国");
        int rows = javaClobDao.updateImageBase64(imageBase64);
        assertThat(rows).isEqualTo(1);

        val newImageBase64s = javaClobDao.queryAll();
        assertThat(newImageBase64s).containsExactly(imageBase64);
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class ImageBase64 {
        private String imageName;
        private String base64;
    }
}
