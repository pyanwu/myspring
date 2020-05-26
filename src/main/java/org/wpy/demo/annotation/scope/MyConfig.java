package org.wpy.demo.annotation.scope;

//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Scope;
import org.wpy.demo.project.entity.Person;

//@Configuration
public class MyConfig {

    /*
         默认单例
        prototype 原型，多例
        singleton 单例(默认单例)
        request   同义次请求只创建一个实例
        session   同一个session只创建一个对象
     */

    /*
        bean的名称和方法名有关，person1(),beanName=person1
     */
//    @Scope("prototype")
//    @Bean
    public Person person(){
        return new Person("wpy",18);
    }
}
