package com.ziwei.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

/**
 * Fixes MyBatis-Plus MapperFactoryBean compatibility issue with Spring 6.x.
 * <p>
 * MyBatis-Plus registers mapper beans with {@code factoryBeanObjectType} set to
 * a String (class name) instead of a Class object. Spring 6.x validates this
 * attribute strictly, causing BeanDefinitionStoreException on startup.
 * This processor converts String values to Class objects for mapper beans.
 * <p>
 * Implements {@link PriorityOrdered} with HIGHEST_PRECEDENCE to run before
 * Spring Boot's {@code DependsOnDatabaseInitializationPostProcessor}.
 *
 * @author JTWORLD
 */
@Component
public class MyBatisPlusCompatProcessor implements BeanFactoryPostProcessor, PriorityOrdered {

    @Override
    public int getOrder() {
        return PriorityOrdered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
            Object factoryBeanObjectType = bd.getAttribute("factoryBeanObjectType");
            if (factoryBeanObjectType instanceof String className) {
                try {
                    Class<?> clazz = Class.forName(className);
                    bd.setAttribute("factoryBeanObjectType", clazz);
                } catch (ClassNotFoundException e) {
                    // Class not on classpath — leave as-is
                }
            }
        }
    }
}
