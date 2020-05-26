package org.wpy.springframework.webmvc.servlet;

import java.util.Map;

/**
 * @Author pywu
 * @Date 2020/5/25 7:45
 **/
public class ModelAndView {
    private String viewName;
    private Map<String,?> model;

    public ModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public ModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }
}
