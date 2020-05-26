package org.wpy.springframework.beans.config;

/**
 * @Author pywu
 * @Date 2020/5/24 21:13
 **/
public class BeanWrapper {
    private Object wrappedInstance;
    private Class<?> wrappedClass;

    public BeanWrapper(Object wrappedInstance){
        this.wrappedClass=wrappedInstance.getClass();
        this.wrappedInstance=wrappedInstance;
    }

    public Object getWrappedInstance(){
        return this.wrappedInstance;
    }

    //返回代理以后的Class  可能是$Proxy0
    public Class<?> getWrappedClass(){
        return  this.wrappedClass;
    }
}
