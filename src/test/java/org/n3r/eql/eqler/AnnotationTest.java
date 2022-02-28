package org.n3r.eql.eqler;

import lombok.val;
import org.junit.Test;
import org.n3r.eql.diamond.Dql;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.generators.Generatable;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AnnotationTest {

    @Test
    public void testAnnotation() {
        val eqlerConfig1 = Generatable.parseEqlerConfig(Test1.class);
        assertNotNull(eqlerConfig1);
        assertEquals(Dql.class, eqlerConfig1.eql());
        assertEquals("MY_DEFAULT", eqlerConfig1.value());
        val eqlerConfig2 = Generatable.parseEqlerConfig(Test2.class);
        assertNotNull(eqlerConfig2);
        assertEquals(Dql.class, eqlerConfig2.eql());
        assertEquals("TEST", eqlerConfig2.value());
    }

    @Documented
    @Inherited
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @EqlerConfig(eql = Dql.class)
    public @interface MyDqler {

        @AliasFor(annotation = EqlerConfig.class)
        String value() default "MY_DEFAULT";
    }

    @MyDqler
    public interface Test1 {}

    @MyDqler("TEST")
    public interface Test2 {}
}
