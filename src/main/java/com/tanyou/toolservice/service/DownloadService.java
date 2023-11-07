package com.tanyou.toolservice.service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;

public interface DownloadService {

    /**
     * 接口类：下载静态资源目录下的文件
     * @param response 浏览器相应
     * @param file 资源文件
     */
    public void resFileDownload(HttpServletResponse response, File file);
}
