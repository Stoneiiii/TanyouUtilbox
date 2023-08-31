package com.antiy.hulei.service.impl;

import com.antiy.hulei.service.UbliboxService;
import com.antiy.hulei.service.UploadService;
import com.antiy.hulei.util.PythonUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
public class UtliboxServiceImpl implements UbliboxService {

    @Autowired
    UploadService uploadService;

    @Override
    public String getPathService(String path) throws IOException {
        String returnValue = null;
        //python路径
        String pythonFilePath = PythonUtils.resolvePythonScriptPath("scripts/find_info_from_cnnvd.py");
        //python路径
        String python = "venv/bin/python3.8";
        //构造python命令
        String line = python + " " + pythonFilePath + " --path src/main/resources/scripts/";
        CommandLine cmdLine = CommandLine.parse(line);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);

        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(streamHandler);
        //执行
        try {
            int exitCode = executor.execute(cmdLine);
            returnValue = outputStream.toString().trim();
            return returnValue;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnValue;
    }


    /**
     * 生成从安卓官网提取补丁命令
     * 配合fix_patch中getpatch环境中使用
     * 提取补丁存放目录：/data/extraPatch
     * @param cveName
     * @param url
     * @return
     */
    @Override
    public String getPatchCmd(String cveName, String url) {
        String creatPatchDirCMD = "mkdir -p /data/extraPatch/";
        String intoPatchDirCMD = "cd /data/androidSource/getPatch/android-13.0.0_r1/";
        String extractPatchCMD = "git checkout ";
        String movePatchCMD = "for name in *.patch; do mv \"$name\" \"/data/extraPatch/";
        creatPatchDirCMD = creatPatchDirCMD +cveName;
        movePatchCMD = movePatchCMD +cveName;
        String pathRegex = "(?<=platform/).*(?=\\+)";
        Pattern pattern = Pattern.compile(pathRegex);
        Matcher matcher = pattern.matcher(url);
        String path = null;
        if (matcher.find()) {
            path = matcher.group();
//            System.out.println(path);
        }else {
            System.out.println("getPatchCmd=>路径不匹配！");
            return null;
        }

        String commitRegex = "(?<=\\+/).*$";
        pattern = Pattern.compile(commitRegex);
        matcher = pattern.matcher(url);
        String commit = null;
        if (matcher.find()) {
            commit = matcher.group();
//            System.out.println(commit);
        }else {
            System.out.println("getPatchCmd=>url不匹配！");
            return null;
        }

        String fileNumCMD = "let b=b=$(find /data/extraPatch/" + cveName + "/" + path +
                " -name \"*.patch\" | wc -l)+1";

        creatPatchDirCMD = creatPatchDirCMD + "/" + path;
        intoPatchDirCMD = intoPatchDirCMD + path;
        extractPatchCMD = extractPatchCMD + commit + " && " + "git format-patch -1 " + commit;
        movePatchCMD = movePatchCMD + "/" + path + "${name/0001/000$b}\"; done\n && gst";

        return creatPatchDirCMD + " && " + fileNumCMD + " && " + intoPatchDirCMD + " && " + extractPatchCMD + " && " + movePatchCMD;
    }

    @Override
    public Map<String, List<String>> parseHtml(MultipartFile multipartFile, String dir, String type) {
        String targetFilePath = uploadService.getUploadFileABSPath(multipartFile, dir);
        System.out.println(targetFilePath);
        //todo: 1. ajax 跳转 上传后页面 2. 后续调用python解析html返回<cvename, url>

        String pythonReturn = null;
        //python路径
        String pythonFilePath = PythonUtils.resolvePythonScriptPath("scripts/get_patch_info.py");
        //python路径
        String python = "venv/bin/python3.8";
        //构造python命令
        String line = python + " " + pythonFilePath + " --path " + targetFilePath + " --type " + type;
        CommandLine cmdLine = CommandLine.parse(line);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);

        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(streamHandler);
        //执行
        try {
            int exitCode = executor.execute(cmdLine);
            pythonReturn = outputStream.toString().trim();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        if(pythonReturn.isEmpty()) {
            System.out.println("error:python脚本输出为空！");
            return null;  
        }
//        System.out.println(pythonReturn);
        
        //格式化接受数据
        Map<String, List<String>> returnMap = new LinkedHashMap<>();
        String[] cveItems = pythonReturn.split("\n");
        for (String cvenameUrlMap : cveItems) {
            String[] cvenameUrlPair = cvenameUrlMap.split("::"); //[0]:cvename [1]:urls(可能多个)
            returnMap.put(cvenameUrlPair[0], new ArrayList<>());
            for (String url : cvenameUrlPair[1].split(",")) {
                String tmp = url.replace("[","")
                        .replace("]","")
                        .replace("'","");
                returnMap.get(cvenameUrlPair[0]).add(tmp);
            }
        }

//        输出测试
//        for (String key : returnMap.keySet()) {
//            System.out.println("key:" + key);
//            for(String url : returnMap.get(key)){
//                System.out.println(url);
//            }
//        }


        return returnMap;
    }
}
