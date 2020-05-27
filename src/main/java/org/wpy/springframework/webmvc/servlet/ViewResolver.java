package org.wpy.springframework.webmvc.servlet;

import java.io.File;

/**
 * @Author pywu
 * @Date 2020/5/25 7:50
 **/
public class ViewResolver {
    private final String DEFAULT_TEMPLATE_SUFFIX=".html";
    private File tempateRootDir;

    public ViewResolver(String templateRoot) {
        String templateRootPath=this.getClass().getClassLoader().getResource(templateRoot).getFile();
        this.tempateRootDir = new File(templateRootPath);
    }

    public View resolveViewName(String viewName){
        if(null == viewName || "".equals(viewName.trim())){
            return null;
        }
        viewName=viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX)?viewName:(viewName+DEFAULT_TEMPLATE_SUFFIX);
        File templateFile=new File((tempateRootDir.getPath()+"/"+viewName).replaceAll("/+","/"));
        return new View(templateFile);
    }
}
