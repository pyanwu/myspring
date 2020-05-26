package org.wpy.springframework.beans.config;

/**
 * @Author pywu
 * @Date 2020/5/24 20:41
 **/
public class BeanDefinition {
    private String beanClassName;
    private String factoryBeanName;

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }
}
