package org.n3r.eql.eqler.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class EqlerApp {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(EqlerConfig.class);
        SpService spEqler = context.getBean(SpService.class);

        System.out.println(spEqler.queryOne());
    }
}
