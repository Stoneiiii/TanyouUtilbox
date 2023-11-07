package com.tanyou.toolservice.service.impl;

import com.tanyou.toolservice.service.DownloadService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;

@Service
public class DownloadServiceImpl implements DownloadService {

    /**
     * 接口实现类：下载静态资源目录下的文件
     * @param response 浏览器相应
     * @param file 资源文件
     */
    @Override
    public void resFileDownload(HttpServletResponse response, File file) {
        //初始化文件流
        FileInputStream fileInputStream = null;
        ServletOutputStream sos = null;

        //转文件为下载流
        try {
            //文件名
            String fileName = file.getName();
            //设置响应头
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            fileInputStream = new FileInputStream(file);
            sos = response.getOutputStream();
            IOUtils.copy(fileInputStream, sos);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("下载失败！");
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (sos != null) {
                    sos.flush();
                    sos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
