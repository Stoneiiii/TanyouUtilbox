package com.antiy.hulei.controller;

import com.antiy.hulei.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/upload")
@Controller
public class UploadController {

    @Autowired
    private UploadService uploadService;

    @PostMapping("/file")
    @ResponseBody
    public String upload(@RequestParam("file") MultipartFile multipartFile, HttpServletRequest request) {

        //空判断
        if (multipartFile.isEmpty()) {
            return "文件为空！！！";
        }

        multipartFile.getSize();//得到大小
        multipartFile.getOriginalFilename();//得到文件名
        String contentType = multipartFile.getContentType();//得到文件类型


        String dir = request.getParameter("dir");
        return uploadService.getUploadFileUrl(multipartFile, dir);
    }
}
