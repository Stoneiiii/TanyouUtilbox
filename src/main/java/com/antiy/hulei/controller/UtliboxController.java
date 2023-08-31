package com.antiy.hulei.controller;

import com.antiy.hulei.service.UbliboxService;
import com.antiy.hulei.service.UploadService;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.jws.WebParam;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/utlibox")
public class UtliboxController {

    @Autowired
    UbliboxService ubliboxService;



    //todo:上传文件
    @GetMapping("/getallfilepath/{path}")
    public List<String> getAllFilePath(@PathVariable String path) {
        return null;
    }

    @PostMapping("/autogetcve")
    public String getCveCmd(@RequestParam("cvename") String cvename,
                                  @RequestParam("url") String url) {
        return ubliboxService.getPatchCmd(cvename, url);
    }

    @PostMapping("/htmlparse")
    @ResponseBody
    public ModelAndView getAllCveCmd(@RequestParam("file") MultipartFile multipartFile,
                                      HttpServletRequest request) {
        //空判断
        if (multipartFile.isEmpty()) {
            System.out.println("上传为空");
            return null;
        }

        String dir = request.getParameter("dir");
        String type = request.getParameter("type");
        System.out.println(type);
        Map<String, List<String>> cvenameUrlMap = ubliboxService.parseHtml(multipartFile, dir, type);
        //todo: 20965 补丁中有多个URL Map 存储不行 只能一个  解决：加计数 key为20965（2）
        Map<String, String> returnValue = new LinkedHashMap<>();
        for (String cvename : cvenameUrlMap.keySet()) {
            int count = 0; //计数url个数：也就是一个cvename 对应多个补丁
            for(String url : cvenameUrlMap.get(cvename)){
                String cmd = ubliboxService.getPatchCmd(cvename, url);
                if (count != 0) {
                    returnValue.put(cvename + '_'+ count, cmd);
                }else {
                    returnValue.put(cvename, cmd);
                }
                count++;
            }
        }
        ModelAndView mv = new ModelAndView();
        mv.addObject("Map", returnValue);
        mv.setViewName("autoextracve::cvetable");
        return mv;
    }

}
