package org.n3r.eql.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class NamesTest {
    @Test
    public void test1() {
        String str = Names.underscoreNameToPropertyName("bean.payType");
        assertThat(str, is(equalTo("bean.paytype")));

        str = Names.underscoreNameToPropertyName("bean_name");
        assertThat(str, is(equalTo("beanName")));

        str = Names.underscoreNameToPropertyName("BEAN_NAME");
        assertThat(str, is(equalTo("beanName")));
    }
}
