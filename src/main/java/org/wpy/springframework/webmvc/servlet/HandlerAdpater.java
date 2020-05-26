package org.wpy.springframework.webmvc.servlet;

import org.wpy.springframework.annotation.RequestParam;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author pywu
 * @Date 2020/5/25 7:47
 **/
public class HandlerAdpater {

    public ModelAndView handler(HttpServletRequest req, HttpServletResponse resp, HandlerMapping handlerMapping) throws InvocationTargetException, IllegalAccessException {
        //保存形参列表
        //将参数名称和参数位置对应的关系保存起来
        Map<String,Integer> paramIndexMapping=new HashMap<String,Integer>();
        //通过运行时的状态去拿注解
        /**
         * query(@RequestParam("name")String name,@Value("age")int age)
         * Annotation[0][0]==>@RequestParam
         * Annotation[1][0]==>@Value
         */
        Annotation[][] pa = handlerMapping.getMethod().getParameterAnnotations();
        for(int i=0;i<pa.length;i++){ //i是参数位置下标
            for(Annotation a:pa[i]){
                if(a instanceof RequestParam){
                    String paramName=((RequestParam)a).value();
                    if(!"".equals(paramName)){
                        paramIndexMapping.put(paramName,i);
                    }
                }
            }
        }
        Class<?>[] parameterTypes = handlerMapping.getMethod().getParameterTypes();
        for(int i=0;i<parameterTypes.length;i++){
            Class<?> parameterType = parameterTypes[i];
            if(parameterType==HttpServletRequest.class || parameterType==HttpServletResponse.class){
                paramIndexMapping.put(parameterType.getName(),i);
            }
        }
        //拼接实参
        //http://localhost:8080/web/query?name=cat&dog&fish
        //String[]主要是考虑复选框传值name=cat&dog&fish
        Map<String,String[]> params=req.getParameterMap();
        Object[] paramValues=new Object[parameterTypes.length];
        for(Map.Entry<String,String[]> param: params.entrySet()){
            //TODO 解析为何物？
            String value= Arrays.toString(params.get(param.getKey()))
                    .replaceAll("\\[|\\]","")
                    .replaceAll("\\s+",",");

            if(!paramIndexMapping.containsKey(param.getKey())){
                continue;
            }
            Integer index = paramIndexMapping.get(param.getKey());
            //允许自定义的类型转换器Converter
            paramValues[index]=castStringValue(value,parameterTypes[index]);
        }
        if(paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
            int index=paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[index]=req;
        }
        if(paramIndexMapping.containsKey(HttpServletResponse.class.getName())){
            int index=paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[index]=resp;
        }
        Object result=handlerMapping.getMethod().invoke(handlerMapping.getController(),paramValues);
        boolean isModelAndView=handlerMapping.getMethod().getReturnType()==ModelAndView.class;
        if(isModelAndView){
            return (ModelAndView)result;
        }
        return null;
    }

    private Object castStringValue(String value, Class<?> parameterType) {
        if(String.class==parameterType){
            return value;
        }else if(Integer.class==parameterType){
            return Integer.valueOf(value);
        }else if(Double.class==parameterType){
            return Double.valueOf(value);
        }else{
            if(value !=null){
                return value;
            }
            return null;
        }
    }
}
