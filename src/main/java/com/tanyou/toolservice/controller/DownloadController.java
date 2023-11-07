package com.tanyou.toolservice.controller;

import com.tanyou.toolservice.service.DownloadService;
import com.tanyou.toolservice.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.File;


@Controller
public class DownloadController {

    @Autowired
    DownloadService downloadService;
    @GetMapping("/download")
    public void test(HttpServletResponse response) {
        String path = CommonUtils.resolveResFilePath("utilbox/tanyoudbbox/sql/2023-11-02/cnnvd_no_sql.txt");
        File file = new File(path);
        downloadService.resFileDownload(response, file);
    }
}
