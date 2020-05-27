package org.wpy.demo.project.controller;

import org.wpy.springframework.annotation.Controller;
import org.wpy.springframework.annotation.RequestMapping;
import org.wpy.springframework.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author pywu
 * @Date 2020/5/27 7:30
 **/
@Controller
@RequestMapping("/test")
public class TestController {

    @RequestMapping("/testMethod")
    public String test(@RequestParam("name") String name, HttpServletRequest req, HttpServletResponse resp){
        return "hello "+name;
    }
}
