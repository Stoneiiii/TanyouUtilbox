package com.antiy.hulei.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    //上传文件，返回文件URl路径
    public String getUploadFileUrl(MultipartFile multipartFile, String dir);

    //上传文件，返回文件绝对路径
    public String getUploadFileABSPath(MultipartFile multipartFile, String dir);
}
