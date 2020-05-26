package org.wpy.springframework.beans.support;

import org.wpy.springframework.beans.config.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @Author pywu
 * @Date 2020/5/24 20:44
 **/
public class BeanDefinitionReader {

    private Properties contextConfig=new Properties();
    //保存扫描的结果
    private List<String> registryBeanClasses=new ArrayList<>();

    public BeanDefinitionReader(String... configLocations){
        doLoadConfig(configLocations[0]);

        //扫描配置文件中相关的类
        doScanner(contextConfig.getProperty("scanPackage"));
    }

    private void doScanner(String scanPackage) {
        //scanPackage=org.wpy.demo存储的是包的路劲
        //转换为文件路劲，实际上就是把.替换为/就OK了
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classPath=new File(url.getFile());
        for (File file:classPath.listFiles()) {
            if(file.isDirectory()){
                doScanner(scanPackage+"."+file.getName());
            }else{
                //只处理.class结尾的
                if(!file.getName().endsWith(".class")){
                    continue;
                }
                String className=scanPackage+"."+file.getName().replace(".class","");
                registryBeanClasses.add(className);
            }
        }
    }

    private void doLoadConfig(String configLocation) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(configLocation.replaceAll("classpath:",""));
        try {
            this.contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null !=is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<BeanDefinition> loaderBeanDefinitions() {
        List<BeanDefinition> result=new ArrayList<BeanDefinition>();
        for(String className:registryBeanClasses){
            try {
                Class<?> beanClass=Class.forName(className);
                //保存类对应的ClassName（全类名）
                //beanName
                //1、默认是类名首字母小写
                String beanName=toLowerFirstCase(beanClass.getSimpleName());//类名首字母小写  App->app
                String beanClassName=beanClass.getName();//类的全限定名 org.wpy.App
                result.add(doCreateBeanDefinition(beanName,beanClassName));
                //2、自定义

                //3、接口注入
                for(Class<?> clazz:beanClass.getInterfaces()){
                    result.add(doCreateBeanDefinition(toLowerFirstCase(clazz.getName()),beanClass.getName()));
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private BeanDefinition doCreateBeanDefinition(String beanName, String beanClassName) {
        BeanDefinition beanDefinition=new BeanDefinition();
        beanDefinition.setBeanClassName(beanClassName);
        beanDefinition.setFactoryBeanName(beanName);
        return beanDefinition;
    }

    private String toLowerFirstCase(String simpleName){
        char[] chars=simpleName.toCharArray();
        chars[0] +=32;
        return String.valueOf(chars);
    }

    public Properties getContextConfig(){
        return this.contextConfig;
    }
}
