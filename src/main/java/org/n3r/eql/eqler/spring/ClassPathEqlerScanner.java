package org.n3r.eql.eqler.spring;

import lombok.val;
import org.n3r.eql.eqler.annotations.Eqler;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Arrays;
import java.util.Set;

public class ClassPathEqlerScanner extends ClassPathBeanDefinitionScanner {
    public ClassPathEqlerScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    /**
     * Configures parent scanner to search for the right interfaces. It can search
     * for all interfaces or just for those that extends a markerInterface or/and
     * those annotated with the annotationClass
     */
    public void registerFilters() {
        addExcludeFilter((metadataReader, metadataReaderFactory) -> !metadataReader.getClassMetadata().isInterface());
        addIncludeFilter(new AnnotationTypeFilter(Eqler.class));
        addIncludeFilter(new AnnotationTypeFilter(EqlerConfig.class));
    }

    /**
     * Calls the parent search that will search and register all the candidates.
     * Then the registered objects are post processed to set them as
     * MapperFactoryBeans
     */
    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        val beanDefinitions = super.doScan(basePackages);

        if (beanDefinitions.isEmpty()) {
            logger.warn("No eqler was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
        } else {
            for (val holder : beanDefinitions) {
                val definition = (GenericBeanDefinition) holder.getBeanDefinition();

                if (logger.isDebugEnabled()) {
                    logger.debug("Creating EqlerFactoryBean with name '" + holder.getBeanName()
                            + "' and '" + definition.getBeanClassName() + "' eqlerInterface");
                }

                // the mapper interface is the original class of the bean
                // but, the actual class of the bean is MapperFactoryBean
                definition.getPropertyValues().add("xyzInterface", definition.getBeanClassName());
                definition.setBeanClass(EqlerScannerRegistrar.EqlerFactoryBean.class);
            }
        }

        return beanDefinitions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return (beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
        if (super.checkCandidate(beanName, beanDefinition)) {
            return true;
        } else {
            logger.warn("Skipping EqlerFactoryBean with name '" + beanName
                    + "' and '" + beanDefinition.getBeanClassName() + "' eqlerInterface"
                    + ". Bean already defined with the same name!");
            return false;
        }
    }

}
