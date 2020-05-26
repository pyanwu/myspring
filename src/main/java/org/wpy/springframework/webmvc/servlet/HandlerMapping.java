package org.wpy.springframework.webmvc.servlet;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * @Author pywu
 * @Date 2020/5/25 7:43
 **/
public class HandlerMapping {
    private Pattern pattern; //URL
    private Method method;   //对应的method
    private Object controller; //对应的controller

    public HandlerMapping(Pattern pattern, Method method, Object controller) {
        this.pattern = pattern;
        this.method = method;
        this.controller = controller;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }
}
