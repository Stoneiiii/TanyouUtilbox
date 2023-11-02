package com.antiy.hulei.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class IndexController {

    //首页
    @RequestMapping({"/", "/index"})
    public String hello(Model model){
        return "main";
    }

    //跳转登录
    @RequestMapping("/toLogin")
    public String utliboxIndex(Model model) {
        model.addAttribute("msg", "请登录！");
        return "login";
    }

    //登录请求
    @RequestMapping("/login")
    public String login(String username, String password, Model model){
        // 获取当前的用户
        Subject subject = SecurityUtils.getSubject();
        // 封装用户的登录数据
        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(username, password);
        // 执行登录的方法，如果没有异常就说明ok
        try{
            subject.login(usernamePasswordToken);
            return "index";
        }catch (UnknownAccountException e){
            model.addAttribute("msg", "用户名错误");
            return "login";
        }catch (IncorrectCredentialsException e){
            model.addAttribute("msg","密码错误");
            return "login";
        }
    }

    //shiro测试
    @RequestMapping("/user/add")
    public String add(){
        return "user/add";
    }

    //shiro测试
    @RequestMapping("/user/update")
    public String update(){
        return "user/update";
    }
}
