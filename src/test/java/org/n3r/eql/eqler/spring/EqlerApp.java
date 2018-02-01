package org.n3r.eql.eqler.spring;

import com.google.common.collect.Lists;
import lombok.val;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.google.common.truth.Truth.assertThat;

public class EqlerApp {
    @Test
    public void test1() {
        System.setProperty("spring.profiles.active", "prod");
        val context = new AnnotationConfigApplicationContext(EqlerConfig.class);
        val spService = context.getBean(SpService.class);

        assertThat(spService.queryOne()).isEqualTo(1);
        assertThat(spService.queryLower()).isEqualTo("o2m");
        assertThat(spService.queryLowers()).isEqualTo(Lists.newArrayList(
                new ABean("a"), new ABean("b")));

        val eqler = context.getBean(SpEqler.class);


        String profileName = eqler.queryProfileName();
        assertThat(profileName).isEqualTo("prodprod");

    }
}
