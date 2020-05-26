package org.wpy.springframework.webmvc.servlet;

import com.sun.org.apache.xerces.internal.jaxp.validation.ErrorHandlerAdaptor;
import org.wpy.springframework.annotation.Controller;
import org.wpy.springframework.annotation.RequestMapping;
import org.wpy.springframework.context.ApplicationContext;
import sun.misc.Request;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 委派模式：
 * 职责：负责任务调度，请求分发
 */
public class DispatcherServlet extends HttpServlet {

    private ApplicationContext applicationContext;
    private List<HandlerMapping> handlerMappings=new ArrayList<HandlerMapping>();
    private Map<HandlerMapping,HandlerAdpater> handlerAdapters=new HashMap<HandlerMapping,HandlerAdpater>();

    private List<ViewResolver> viewResolvers=new ArrayList<ViewResolver>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            this.doDispatch(req,resp);
        } catch (Exception e) {
            //500 error
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //1、通过request拿到URL，去匹配一个HandlerMapping
        HandlerMapping handlerMapping=getHandlerMapping(req);
        if(handlerMapping==null){
            //404
            return;
        }
        //2、根据HandlerMapping获得一个HandlerAdapter
        HandlerAdpater handlerAdpater=getHandlerAdapter(handlerMapping);

        //3、解析某一个方法的形参和返回值之后，统一封装为ModelAndView对象
        ModelAndView mv=handlerAdpater.handler(req,resp,handlerMapping);

        //真正的输出,把ModelAndView变成一个ViewResolver
        processDispatchResult(req,resp,mv);
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, ModelAndView mv) throws Exception{
        if(null == mv){
            return;
        }
        if(this.viewResolvers.isEmpty()){
            return;
        }
        for(ViewResolver viewResolver:this.viewResolvers){
            View view = viewResolver.resolveViewName(mv.getViewName());
            view.render(mv.getModel(),req,resp);
        }
    }

    private HandlerAdpater getHandlerAdapter(HandlerMapping handlerMapping) {
        if(this.handlerAdapters.isEmpty()){
            return null;
        }
        return this.handlerAdapters.get(handlerMapping);
    }

    /**
     * 访问    http://localhost:8080/news/main/list.jsp
     * req.getRequestURI() ==>  /news/main/list.jsp
     * req.getRequestURL() ==>   http://localhost:8080/news/main/list.jsp
     * req.getContextPath()==>  /news
     * req.getServletPath()==> /main/list.jsp
     */
    private HandlerMapping getHandlerMapping(HttpServletRequest req) {
        if(this.handlerMappings.isEmpty()){
            return null;
        }

        String url=req.getRequestURI();
        String contextPath=req.getContextPath();
        url=url.replaceAll(contextPath,"").replaceAll("/+","/");

        for(HandlerMapping handlerMapping:handlerMappings){
            Matcher matcher = handlerMapping.getPattern().matcher(url);
            if(!matcher.matches()){
                continue;
            }
            return handlerMapping;
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        applicationContext=new ApplicationContext(config.getInitParameter("contextConfigLocation"));

        //初始化九大组件
        initStrategies(applicationContext);


    }

    public void initStrategies(ApplicationContext applicationContext){

        //多文件上传的组件
//        initMultipartResolver(applicationContext);
        //初始化本地语言环境
//        initLocaleResolver(applicationContext);
        //初始化模板处理器
//        initThemeResolver(applicationContext);

        initHandlerMapping(applicationContext);

        initHandlerAdapter(applicationContext);
        //初始化异常拦截器
//        initHandlerExceptionResolvers(applicationContext);
        //初始化视图预处理器
//        initRequestToViewNameResolvers(applicationContext);
        //初始化试图转换器
        initViewResolvers(applicationContext);
        //FlashMap管理器
//        initFlashMapManager(applicationContext);
    }

    private void initFlashMapManager(ApplicationContext applicationContext) {
    }

    private void initHandlerExceptionResolvers(ApplicationContext applicationContext) {
    }

    private void initRequestToViewNameResolvers(ApplicationContext applicationContext) {
    }

    private void initThemeResolver(ApplicationContext applicationContext) {
    }

    private void initLocaleResolver(ApplicationContext applicationContext) {
    }

    private void initMultipartResolver(ApplicationContext applicationContext) {
    }

    private void initViewResolvers(ApplicationContext applicationContext) {
        try{
            String templateRoot=applicationContext.getConfig().getProperty("templateRoot");
            String templateRootPath=this.getClass().getClassLoader().getResource(templateRoot).getFile();
            File templateRootDir=new File(templateRootPath);
            for(File file:templateRootDir.listFiles()){
                this.viewResolvers.add(new ViewResolver(templateRoot));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initHandlerAdapter(ApplicationContext applicationContext) {
        for(HandlerMapping handlerMapping:handlerMappings){
            this.handlerAdapters.put(handlerMapping,new HandlerAdpater());
        }
    }

    private void initHandlerMapping(ApplicationContext applicationContext) {
        if(this.applicationContext.getBeanDefinitionCount()==0){
            return;
        }
        for(String beanName:this.applicationContext.getBeanDefinitionNames()){
            Object instance = applicationContext.getBean(beanName);
            Class<?> clazz = instance.getClass();

            if(!clazz.isAnnotationPresent(Controller.class)){
                continue;
            }

            //取Controller上的url
            String baseUrl="";
            if(clazz.isAnnotationPresent(RequestMapping.class)){
                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //取Method上的url,只获取public的Method
            for(Method method:clazz.getMethods()){
                if(!method.isAnnotationPresent(RequestMapping.class)){
                    continue;
                }

                RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
                //方法上的url
                String methodUrl = methodRequestMapping.value();
                String regex=("/"+baseUrl+methodUrl.replaceAll("\\*",".*").replaceAll("/+","/"));
                Pattern pattern= Pattern.compile(regex);
                handlerMappings.add(new HandlerMapping(pattern,method,instance));
                System.out.println("Mapped: "+regex+", "+method);
            }
        }
    }
}
