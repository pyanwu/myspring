package org.wpy.demo.annotation.scope;


import org.junit.Test;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.wpy.demo.project.entity.Person;

public class Mytest {

//    @Test
//    public void test(){
//        ApplicationContext app=new AnnotationConfigApplicationContext(MyConfig.class);
//        Object person = app.getBean("person");
//        Object person2 = app.getBean("person");
//        System.out.println(person==person2);
//    }

    @Test
    public void test2(){
        Class<?>[] interfaces = Person.class.getInterfaces();
        for(Class<?> c:interfaces){
            System.out.println(c.getName());
            System.out.println(c.getSimpleName());
        }
        System.out.println(Person.class.getName());
        System.out.println(Person.class.getSimpleName());
    }
}
