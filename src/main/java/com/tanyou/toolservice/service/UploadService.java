package com.tanyou.toolservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    //上传文件，返回文件URl路径
    public String getUploadFileUrl(MultipartFile multipartFile, String dir);

    //上传文件，返回文件绝对路径
    /**
     * 接口类：接受文件，和保存路径，返回文件在服务器中的绝对路径
     * @param multipartFile 文件
     * @param dir 想要保存的路径
     * @return 文件在服务器中的绝对路径
     */
    public String getUploadFileABSPath(MultipartFile multipartFile, String dir);
}
