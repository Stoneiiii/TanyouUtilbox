package com.antiy.hulei.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Controller
public class IndexController {

    @RequestMapping({"/", "/index"})
    public String hello(Model model){
        model.addAttribute("msg", "首页");
        return "index";
    }

    @RequestMapping("/main")
    public String utliboxIndex(Model model) {
        return "main";
    }
}
