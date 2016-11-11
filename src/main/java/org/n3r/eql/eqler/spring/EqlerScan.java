package org.n3r.eql.eqler.spring;

import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(EqlerScannerRegistrar.class)
public @interface EqlerScan {

    /**
     * Alias for the {@link #basePackages()} attribute. Allows for more concise
     * annotation declarations.
     * @return base packages
     */
    String[] value() default {};

    /**
     * Base packages to scan for MyBatis interfaces. Note that only interfaces
     * with at least one method will be registered; concrete classes will be
     * ignored.
     * @return base packages
     */
    String[] basePackages() default {};

    /**
     * Type-safe alternative to {@link #basePackages()} for specifying the packages
     * to scan for annotated components. The package of each class specified will be scanned.
     * <p>Consider creating a special no-op marker class or interface in each package
     * that serves no purpose other than being referenced by this attribute.
     * @return base package classes
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * The {@link BeanNameGenerator} class to be used for naming detected components
     * within the Spring container.
     * @return name generator
     */
    Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;
}
