package com.tanyou.toolservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.file.Paths;

@RequestMapping("/todir")
@Controller
public class MyFileExplorerController {

    @Value("${file.utilboxFileRootPath}")
    String utilboxFileRootPath;


    /**
     * 跳转文件浏览器中，db位置
     *
     * @return file-explorer
     */
    @RequestMapping("/tanyoudbbox-db")
    public String toTanyoudbboxDbDir() {
        String dbPath = Paths.get(utilboxFileRootPath, "tanyoudbbox", "db").toString();
        return "redirect:/file-explorer?dir=" + dbPath;
    }

    /**
     * 跳转文件浏览器中，dic位置
     *
     * @return file-explorer
     */
    @RequestMapping("/tanyoudbbox-dic")
    public String toTanyoudbboxDicDir() {
        String dicPath = Paths.get(utilboxFileRootPath, "tanyoudbbox", "dic").toString();
        return "redirect:/file-explorer?dir=" + dicPath;
    }

    /**
     * 跳转文件浏览器中，cnnvd位置
     *
     * @return file-explorer
     */
    @RequestMapping("/tanyoudbbox-cnnvd")
    public String toTanyoudbboxCnnvdDir() {
        String cnnvdPath = Paths.get(utilboxFileRootPath, "tanyoudbbox", "cnnvd").toString();
        return "redirect:/file-explorer?dir=" + cnnvdPath;
    }

    /**
     * 跳转文件浏览器中，sql位置
     *
     * @return file-explorer
     */
    @RequestMapping("/tanyoudbbox-sql")
    public String toTanyoudbboxSqlDir() {
        String sqlPath = Paths.get(utilboxFileRootPath, "tanyoudbbox", "sql").toString();
        return "redirect:/file-explorer?dir=" + sqlPath;
    }
}
