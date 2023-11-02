package com.antiy.hulei.service.impl;

import com.antiy.hulei.service.UtilboxService;
import com.antiy.hulei.service.UploadService;
import com.antiy.hulei.util.CommonUtils;
import com.antiy.hulei.util.Unzip;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
public class UtliboxServiceImpl implements UtilboxService {

    @Autowired
    UploadService uploadService;

    @Autowired
    ResourceLoader resourceLoader;

    /**
     * 接口实现类：返回路径下所有文件的路径
     * @param path 文件夹路径
     * @return String[]: 所有文件的路径
     */
    @Override
    public String[] getPathService(String path) {
        String returnValue = null;

        //python路径
        String pythonFilePath = CommonUtils.resolveResFilePath("scripts/find_info_from_cnnvd.py");
        //python路径
        String python = "venv/bin/python3.8";
        //构造python命令
        String line = python + " " + pythonFilePath + " --path " + path;
        CommandLine cmdLine = CommandLine.parse(line);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);

        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(streamHandler);
        //执行
        try {
            int exitCode = executor.execute(cmdLine);

            returnValue = outputStream.toString().trim()
                    .replace("[", "")
                    .replace("]", "")
                    .replace("'", "")
                    .replace(" ", "");
            return returnValue.split(",");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new String[0];
    }


    /**
     * 接口的实现类：接受CVE编号和对应的官方url信息，生成从安卓官网提取补丁命令
     * 配合fix_patch中getpatch环境中使用
     * 提取补丁存放目录：/data/extraPatch
     * @param cveName CVE编号
     * @param url 官方url信息
     * @return String: 一句话提取官方补丁CMD
     */
    @Override
    public String getPatchCmd(String cveName, String url) {
        // 拼接CMD准备
        String creatPatchDirCMD = "mkdir -p /data/extraPatch/";
        String intoPatchDirCMD = "cd /data/androidSource/getPatch/android-13.0.0_r1/";
        String extractPatchCMD = "git checkout ";
        String movePatchCMD = "for name in *.patch; do mv \"$name\" \"/data/extraPatch/";
        creatPatchDirCMD = creatPatchDirCMD +cveName;
        movePatchCMD = movePatchCMD +cveName;
        // 从url中提取补丁打入路径
        String pathRegex = "(?<=platform/).*(?=\\+)";
        Pattern pattern = Pattern.compile(pathRegex);
        Matcher matcher = pattern.matcher(url);
        String path = "";
        if (matcher.find()) {
            path = matcher.group();
        }else {
            System.out.println("getPatchCmd=>路径不匹配！");
            return "";
        }
        // 从url中提取补丁的commit编号
        String commitRegex = "(?<=\\+/).*$";
        pattern = Pattern.compile(commitRegex);
        matcher = pattern.matcher(url);
        String commit = "";
        if (matcher.find()) {
            commit = matcher.group();
        }else {
            System.out.println("getPatchCmd=>url不匹配！");
            return "";
        }
        // 获得当前CVE编号同一目录下有的补丁数量：有的CVE同一目录下有多个补丁。
        String fileNumCMD = "let b=b=$(find /data/extraPatch/" + cveName + "/" + path +
                " -name \"*.patch\" | wc -l)+1";
        // 把CMD的各个部分组合起来。
        creatPatchDirCMD = creatPatchDirCMD + "/" + path;
        intoPatchDirCMD = intoPatchDirCMD + path;
        extractPatchCMD = extractPatchCMD + commit + " && " + "git format-patch -1 " + commit;
        movePatchCMD = movePatchCMD + "/" + path + "${name/0001/000$b}\"; done\n && gst";
        //加提取成功后，显示成功的信息
        String resultCMD = " && echo -e \"\\033[43;32m 提取成功！！\\033[0m\"";
        // 最终组合CMD并返回
        return creatPatchDirCMD + " && " + fileNumCMD + " && " + intoPatchDirCMD + " && " + extractPatchCMD + " && " + movePatchCMD + resultCMD;
    }


    /**
     * 接口的实现类：传入谷歌每月发布Android漏洞页面下载后的html文件，文件名，需要提取的Android漏洞类型
     * 调用python脚本解析文件内容，返回一个Map<cveName, 对应的一句话提取CMD（可能多条）>
     * @param multipartFile 传入谷歌每月发布Android漏洞页面下载后的html文件
     * @param dir 文件名
     * @param type 需要提取的Android漏洞类型
     * @return 返回一个Map<cveName, 对应的一句话提取CMD（可能多条）>
     */
    @Override
    public Map<String, List<String>> parseHtml(MultipartFile multipartFile, String dir, String type) {
        // 获得上传文件的绝对路径
        String targetFilePath = uploadService.getUploadFileABSPath(multipartFile, dir);
//        System.out.println(targetFilePath);

        String pythonReturn = null;
        //生成CMD命令的python脚本路径
        String pythonFilePath = CommonUtils.resolveResFilePath("scripts/get_patch_info.py");
        //python环境路径
        String pythonEnv = "venv/bin/python3.8";
        //构造python命令：python环境 + python脚本 + 传入参数
        String line = pythonEnv + " " + pythonFilePath + " --path " + targetFilePath + " --type " + type;
        //解析成CMD命令
        CommandLine cmdLine = CommandLine.parse(line);

        //初始化执行CMD环境
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);

        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(streamHandler);
        //执行CMD
        try {
            int exitCode = executor.execute(cmdLine);   //exitCode可以用来判断是否执行成功
            //获得CMD运行后的返回值
            pythonReturn = outputStream.toString().trim();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        //CMD返回为异常判断
        if(pythonReturn.isEmpty()) {
            System.out.println("error:python脚本输出为空！");
            return null;  
        }

        //用Map格式化CMD返回的数据
        Map<String, List<String>> returnMap = new LinkedHashMap<>();
        String[] cveItems = pythonReturn.split("\n");
        for (String cveNameUrlMap : cveItems) {
            String[] cveNameUrlPair = cveNameUrlMap.split("::"); //[0]:cveName [1]:urls(可能多个)
            returnMap.put(cveNameUrlPair[0], new ArrayList<>());
            for (String url : cveNameUrlPair[1].split(",")) {
                String tmp = url.replace("[","")
                        .replace("]","")
                        .replace("'","");
                returnMap.get(cveNameUrlPair[0]).add(tmp);
            }
        }

//        输出测试
//        for (String key : returnMap.keySet()) {
//            System.out.println("key:" + key);
//            for(String url : returnMap.get(key)){
//                System.out.println(url);
//            }
//        }

        //TODO: 判断影响安卓版本包含13？因为有些补丁并没有影响13
        return returnMap;
    }



    /**
     * 接口实现类：接收cnnvd下载数据的压缩包，调用python脚本，生成字典文件
     * 字典文件路径：classpath:/utilbox/tanyoudbbox/dic/dic.txt
     * @param multipartFile cnnvd压缩包文件
     * @return 是否生成字典成功
     */
    @Override
    public Boolean genCnnvdDic(MultipartFile multipartFile) throws IOException {
        // 获得上传文件的绝对路径
        String dir = "cnnvd";
        String targetFilePath = uploadService.getUploadFileABSPath(multipartFile, dir);
        System.out.println(targetFilePath);

        //解压zip文件：获得目标文件
        File zipFile = new File(targetFilePath);
        //获得存放cnnvd的xml的静态资源路径
        String utilboxResCnnvdPath = CommonUtils.resolveResDirPath("utilbox/tanyoudbbox/cnnvd");
        //如果有文件，则删除
        FileSystemUtils.deleteRecursively(new File(utilboxResCnnvdPath));

        //解压zip文件：获得解压路径并解压
        File unzipFile = new File(utilboxResCnnvdPath);
        Unzip.zipDecompress(zipFile, unzipFile);

        //todo:把运行python脚本抽象成工具类中去
        String pythonReturn = null;
        //生成CMD命令的python脚本路径
        String pythonFilePath = CommonUtils.resolveResFilePath("scripts/cnnvd_data_process.py");
        //python环境路径
        String pythonEnv = "venv/bin/python3.8";
        //构造python命令：python环境 + python脚本 + 传入参数
        //获得dic存放路径
        String dicPath = CommonUtils.resolveResDirPath("utilbox/tanyoudbbox/dic") + "/dic.txt";
        String line = pythonEnv + " " + pythonFilePath + " --file_path " + unzipFile.getPath() + " --dic_path " + dicPath;
        //解析成CMD命令
        CommandLine cmdLine = CommandLine.parse(line);

        //初始化执行CMD环境
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);

        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(streamHandler);
        //执行CMD
        try {
            int exitCode = executor.execute(cmdLine);   //exitCode可以用来判断是否执行成功
            //获得CMD运行后的返回值
            pythonReturn = outputStream.toString().trim();
            System.out.println(pythonReturn);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        //CMD返回为异常判断
        if(pythonReturn.isEmpty()) {
            System.out.println("error:python脚本输出为空！");
            return false;
        }
        return true;
    }

    /**
     * 接口实现类：保存上传的探优数据库db文件到静态目录utilbox/tanyoudbbox/db
     * @param multipartFile 探优数据库文件
     * @return String: 是否文件MD5
     */
    @Override
    public String storeTanyoudb(MultipartFile multipartFile) throws IOException {
        // 获得上传文件的绝对路径
        String dir = "db";
        String targetFilePath = uploadService.getUploadFileABSPath(multipartFile, dir);
        System.out.println(targetFilePath);

        //复制db文件到资源路径
        String resDbPath = CommonUtils.resolveResDirPath("utilbox/tanyoudbbox/db");
        String targetDicPath = resDbPath + File.separator + "tanyoudb.db";
        CommonUtils.copyFile(targetFilePath, targetDicPath);

        //计算文件MD5返回
        return CommonUtils.getFileMD5(targetDicPath);
    }

    /**
     * 接口实现类：是否在资源文件目录存在探优db
     * @return Boolean:是否存在
     */
    @Override
    public Boolean isTanyoudbExist() throws IOException {
        return CommonUtils.isResFileExist("utilbox/tanyoudbbox/db/tanyoudb.db");
    }

    /**
     * 接口实现类：是否在资源文件目录存在字典文件
     * @return Boolean:是否存在
     */
    @Override
    public Boolean isDicExist() {
        return CommonUtils.isResFileExist("utilbox/tanyoudbbox/dic/dic.txt");
    }

    /**
     * 接口实现类：探优数据库校对该字段,在结果目录utilbox/tanyoudbbox/out里面生成sql语句
     * @param field 探优数据库字段
     */
    @Override
    public String modifyTanyoudbField(String field) throws IOException {
        String tanyoudbPath = CommonUtils.resolveResFilePath("utilbox/tanyoudbbox/db/tanyoudb.db");
        String dicPath = CommonUtils.resolveResFilePath("utilbox/tanyoudbbox/dic/dic.txt");
        System.out.println("Python 脚本正则运行中");
        //todo:把运行python脚本抽象成工具类中去
        String pythonReturn = null;
        //生成CMD命令的python脚本路径
        String pythonFilePath = CommonUtils.resolveResFilePath("scripts/modify_tanyoudb.py");
        //python脚本选择运行那个功能：option的值
        String option = "1";
        //python环境路径
        String pythonEnv = "venv/bin/python3.8";
        //构造python命令：python环境 + python脚本 + 传入参数
        String line = pythonEnv + " " + pythonFilePath
                + " " + option
                + " --db_path " + tanyoudbPath
                + " --col_name " + field
                + " --dic_path " + dicPath;
        //解析成CMD命令
        CommandLine cmdLine = CommandLine.parse(line);

        //初始化执行CMD环境
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);

        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(streamHandler);
        //执行CMD
        try {
            int exitCode = executor.execute(cmdLine);   //exitCode可以用来判断是否执行成功
            //获得CMD运行后的返回值
            pythonReturn = outputStream.toString().trim();
            System.out.println(pythonReturn);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        //CMD返回为异常判断
        if(pythonReturn.isEmpty()) {
            System.out.println("error:python脚本输出为空！");
            return "";
        }else{
            //把生成的SQL和没有找到的cve_notfound.txt复制到静态资源utilbox/tanyoudbbox/SQL/时间 路径。
            //获取当前时间，并用把时间加入路径
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String datePath = dateFormat.format(new Date());
            String targetDir = CommonUtils.resolveResDirPath("utilbox/tanyoudbbox/SQL" + File.separator + datePath);

            String sqlAbsPath = CommonUtils.resolveFilePath(field + "_sql.txt");
            String sqlTargetPath = targetDir + File.separator + field + "_sql.txt";
            //复制并删除
            CommonUtils.copyFile(sqlAbsPath, sqlTargetPath);
            //其他文件
            if (CommonUtils.isFileExist("cve_notfound.txt")) {
                String cve_notfoundAbsPath = CommonUtils.resolveFilePath("cve_notfound.txt");
                String cve_notfoundTargetPath = targetDir + File.separator + "cve_notfound.txt";
                CommonUtils.copyFile(cve_notfoundAbsPath, cve_notfoundTargetPath);
            }
            if (CommonUtils.isFileExist("cve_multidata.txt")) {
                String cve_multidataAbsPath = CommonUtils.resolveFilePath("cve_multidata.txt");
                String cve_multidataTargetPath = targetDir + File.separator + "cve_multidata.txt";
                CommonUtils.copyFile(cve_multidataAbsPath, cve_multidataTargetPath);
            }

            return pythonReturn;
        }
    }


}
