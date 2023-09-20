package com.antiy.hulei.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface UbliboxService {

    //获得一个路径下所有文件的路径
    public String getPathService(String path) throws IOException;

    //生产在服务器中提取官方补丁的命令
    public String getPatchCmd(String cveName, String url);

    //解析上传的html，获得cvename和url
    public Map<String, List<String>> parseHtml(MultipartFile multipartFile, String dir, String type);
}
