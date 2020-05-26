package org.wpy.springframework.context;

import org.wpy.springframework.annotation.Autowired;
import org.wpy.springframework.annotation.Controller;
import org.wpy.springframework.annotation.Service;
import org.wpy.springframework.beans.config.BeanDefinition;
import org.wpy.springframework.beans.config.BeanWrapper;
import org.wpy.springframework.beans.support.BeanDefinitionReader;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 完成Bean的创建
 * @Author pywu
 * @Date 2020/5/24 20:18
 **/
public class ApplicationContext {

    private BeanDefinitionReader reader;

    //存储注册信息的BeanDefinition
    private Map<String, BeanDefinition> beanDefinitionMap=new ConcurrentHashMap<String,BeanDefinition>();
    //单例的IOC容器缓存
    private Map<String,Object> factoryBeanObjectCache=new ConcurrentHashMap<String,Object>();
    //通用的IOC容器
    private Map<String,BeanWrapper> factoryBeanInstanceCache=new ConcurrentHashMap<String, BeanWrapper>();



    public ApplicationContext(String... configLocations){
        try{
            //1、定位配置文件、读取配置文件
            reader=new BeanDefinitionReader(configLocations);

            //2、加载解析配置文件,封装为BeanDefinition
            List<BeanDefinition> beanDefinitions=reader.loaderBeanDefinitions();
            //3、注册，把配置信息放到容器里，（伪IOC容器）
            doRegistBeanDefinition(beanDefinitions);

            //4、完成自动依赖注入
            doAutowired();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void doAutowired() {
        //调用getBean才触发
        //这一步，所有的Bean并没有真正的实例化，还只是配置阶段
        for(Map.Entry<String,BeanDefinition> beanDefinitionEntry:beanDefinitionMap.entrySet()){
            String beanName=beanDefinitionEntry.getKey();
            getBean(beanName);
        }
    }

    private void doRegistBeanDefinition(List<BeanDefinition> beanDefinitions) throws Exception {
        for(BeanDefinition beanDefinition:beanDefinitions){
            if(beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
                throw new Exception("the "+beanDefinition.getFactoryBeanName()+"is exists!");
            }
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
            beanDefinitionMap.put(beanDefinition.getBeanClassName(),beanDefinition);
        }
    }

    //Bean的实例化，DI是从这个方法开始的
    public Object getBean(String beanName){
        //拿到BeanDefinition的配置信息
        BeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        //反射实例化newInstance();
        Object instance=instantiateBean(beanName,beanDefinition);
        //包装成一个BeanWrapper对象
        BeanWrapper beanWrapper=new BeanWrapper(instance);
        //保存到IOC容器
        factoryBeanInstanceCache.put(beanName,beanWrapper);
        //执行依赖注入
        populateBean(beanName,beanDefinition,beanWrapper);

        return beanWrapper.getWrappedInstance();
    }

    private void populateBean(String beanName, BeanDefinition beanDefinition, BeanWrapper beanWrapper) {
        //可能会涉及到循环依赖？
        //解决方案：1、把第一次读取结果为空的BeanDefinition存到第一个缓存
        //         2、等第一次循环之后，第二次循环在检查第一次的缓存，在进行赋值
        Object instance=beanWrapper.getWrappedInstance();

        Class<?> clazz = beanWrapper.getWrappedClass();

        //不是谁都可以注入
        if(!(clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class))){
            return ;
        }
        
        for(Field field:clazz.getDeclaredFields()){
            if(!field.isAnnotationPresent(Autowired.class)){
                continue;
            }
            Autowired autowired = field.getAnnotation(Autowired.class);
            String autowireBeanName = autowired.value().trim();
            //如果用户没有自定义注入的beanName,就默认根据类型注入
            if("".equals(autowireBeanName)){
                autowireBeanName = field.getType().getName();
            }
            //暴力访问
            field.setAccessible(true);

            try {
                field.set(instance,this.factoryBeanInstanceCache.get(autowireBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    //创建真正的实例对象
    private Object instantiateBean(String beanName, BeanDefinition beanDefinition) {
        String beanClassName = beanDefinition.getBeanClassName();
        Object instance=null;
        try {
            if(this.factoryBeanObjectCache.containsKey(beanName)){
                instance=this.factoryBeanObjectCache.get(beanName);
            }else {
                Class<?> clazz = Class.forName(beanClassName);
                try {
                    instance = clazz.newInstance();
                    this.factoryBeanObjectCache.put(beanName, instance);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return instance;
    }

    public Object getBean(Class beanClass){
        return getBean(beanClass.getName());
    }

    public int getBeanDefinitionCount(){
        return this.beanDefinitionMap.size();
    }

    public String[] getBeanDefinitionNames(){
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public Properties getConfig(){
        return this.reader.getContextConfig();
    }
}
