package org.n3r.eql.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class EqlUtilsTest {
    @Test
    public void test1() {
        String str = EqlUtils.convertUnderscoreNameToPropertyName("bean.payType");
        assertThat(str, is(equalTo("bean.paytype")));

        str = EqlUtils.convertUnderscoreNameToPropertyName("bean_name");
        assertThat(str, is(equalTo("beanName")));

        str = EqlUtils.convertUnderscoreNameToPropertyName("BEAN_NAME");
        assertThat(str, is(equalTo("beanName")));
    }
}
