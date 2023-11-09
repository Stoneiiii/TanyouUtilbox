package com.tanyou.toolservice.service.impl;

import com.tanyou.toolservice.service.UtilboxService;
import com.tanyou.toolservice.service.UploadService;
import com.tanyou.toolservice.util.*;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
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

    @Value("${file.utilboxFileRootPath}")
    String utilboxFileRootPath;

    /**
     * （暂时没用）获得文件夹下所有文件的路径
     * @param path 文件夹路径
     * @return String[]所有文件的路径
     */
    @Override
    public String[] getPathService(String path) {
        //todo:使用的话 需要把pothon脚本调用那块代码抽取出来到pythonutil层
        String returnValue = null;

        //python路径
        String pythonFilePath = CommonUtils.resolveResFilePath("scripts/find_info_from_cnnvd.py");
        //python路径
        String python = "venv/bin/python3.8";
        //构造python命令
        String line = python + " " + pythonFilePath + " --path " + path;
        CommandLine cmdLine = CommandLine.parse(line);
        //初始化CMD
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
     *
     * @param cveName CVE编号
     * @param url     官方url信息
     * @param androidVersion 补丁最高的安卓版本号
     * @return String: 一句话提取官方补丁CMD
     */
    @Override
    public String getPatchCmd(String cveName, String url, String androidVersion) {
        //todo: 把一些变量抽取出成配置文件。

        // 拼接CMD准备
        String creatPatchDirCMD = "mkdir -p /data/extraPatch/";
        //判断Android版本号
        if (TanyouSvrAndroidVersionEnum.getTanyouAndroidVersion(androidVersion) == null) {
            return String.format("Android%s版本还不支持提取！", androidVersion);
        }
        String intoPatchDirCMD = String.format("cd /data/androidSource/getPatch/%s/", TanyouSvrAndroidVersionEnum.getTanyouAndroidVersion(androidVersion));
        String extractPatchCMD = "git checkout ";
        String movePatchCMD = "for name in *.patch; do mv \"$name\" \"/data/extraPatch/";
        creatPatchDirCMD = creatPatchDirCMD + cveName;
        movePatchCMD = movePatchCMD + cveName;
        // 从url中提取补丁打入路径
        String pathRegex = "(?<=platform/).*(?=\\+)";
        Pattern pattern = Pattern.compile(pathRegex);
        Matcher matcher = pattern.matcher(url);
        String path = "";
        if (matcher.find()) {
            path = matcher.group();
        } else {
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
        } else {
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
        movePatchCMD = movePatchCMD + "/" + path + "${name/0001/000$b}\"; done && gst";
        //加提取成功后，显示成功的信息
        String resultCMD = String.format(" && echo -e \"\\033[43;32m %s提取成功！！\\033[0m\"", cveName);
        // 最终组合CMD并返回
        return creatPatchDirCMD + " && " + fileNumCMD + " && " + intoPatchDirCMD + " && " + extractPatchCMD + " && " + movePatchCMD + resultCMD;
    }


    /**
     * 接口的实现类：传入谷歌每月发布Android漏洞页面下载后的html文件，文件名，需要提取的Android漏洞类型
     * 调用python脚本解析文件内容，返回一个Map<cveName, 对应的一句话提取CMD（可能多条）>
     *
     * @param multipartFile 传入谷歌每月发布Android漏洞页面下载后的html文件
     * @param dir           文件名
     * @param type          需要提取的Android漏洞类型
     * @return 返回一个Map<cveName, 对应的一句话提取CMD （ 可能多条 ）>
     */
    @Override
    public Map<String, List<String>> parseHtml(MultipartFile multipartFile, String dir, String type) {
        //TODO: 有一行出现2个CVE编号的情况，只能适配一个CVE，应该为每个CVE编号提取补丁，并判断为同一个：2023.7月CVE-2022-27405, CVE-2022-27406

        // 获得上传文件的绝对路径
        String targetFilePath = uploadService.getUploadFileABSPath(multipartFile, dir);

        //调用python脚本
        String pythonReturn = PythonUtils.getPatchInfo(targetFilePath, type);

        //用Map格式化CMD返回的数据
        Map<String, List<String>> returnMap = new LinkedHashMap<>();
        assert pythonReturn != null;
        String[] cveItems = pythonReturn.split("\n");
        for (String cveNameUrlMap : cveItems) {
            String[] cveNameUrlPair = cveNameUrlMap.split("::"); //[0]:cveName [1]:urls(可能多个)
            returnMap.put(cveNameUrlPair[0], new ArrayList<>());
            for (String url : cveNameUrlPair[1].split(",")) {
                String tmp = url.replace("[", "")
                        .replace("]", "")
                        .replace("'", "");
                returnMap.get(cveNameUrlPair[0]).add(tmp);
            }
            returnMap.get(cveNameUrlPair[0]).add(cveNameUrlPair[2]);
        }

        return returnMap;
    }


    /**
     * 接口实现类：接收cnnvd下载数据的压缩包，调用python脚本，生成字典文件
     * 字典文件路径：classpath:/utilbox/tanyoudbbox/dic/dic.txt
     *
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
        //解压zip文件：获得解压路径
        File unzipFile = new File(utilboxFileRootPath, "tanyoudbbox" + File.separator + "cnnvd");
        //如果有文件，则删除
        FileSystemUtils.deleteRecursively(unzipFile);
        //解压
        Unzip.zipDecompress(zipFile, unzipFile);

        //调用python脚本
        File dicDirPath = new File(utilboxFileRootPath + Paths.get("tanyoudbbox", "dic").toString());
        if (!dicDirPath.exists()) dicDirPath.mkdirs();

        return PythonUtils.cnnvdDataProcess(dicDirPath.getPath(), unzipFile.getPath());
    }

    /**
     * 接口实现类：保存上传的探优数据库db文件到静态目录utilbox/tanyoudbbox/db
     *
     * @param multipartFile 探优数据库文件
     * @return String: 是否文件MD5
     */
    @Override
    public String storeTanyoudb(MultipartFile multipartFile) throws IOException {
        // 获得上传文件的绝对路径
        String dir = "db";
        String targetFilePath = uploadService.getUploadFileABSPath(multipartFile, dir);
        System.out.println(targetFilePath);

        //复制到探优DB工具箱结果目录中
        File dbDirPath = new File(utilboxFileRootPath, Paths.get("tanyoudbbox", "db").toString());
        if (!dbDirPath.exists()) dbDirPath.mkdirs();
        String DicPath = dbDirPath.getPath() + File.separator + "tanyoudb.db";
        CommonUtils.copyFile(targetFilePath, DicPath);

        //计算文件MD5返回
        return CommonUtils.getFileMD5(DicPath);
    }

    /**
     * 接口实现类：是否在资源文件目录存在探优db
     *
     * @return Boolean:是否存在
     */
    @Override
    public Boolean isTanyoudbExist() throws IOException {
        return CommonUtils.isFileExist(Paths.get(utilboxFileRootPath, "tanyoudbbox", "db", "tanyoudb.db").toString());
    }

    /**
     * 接口实现类：是否在资源文件目录存在字典文件
     *
     * @return Boolean:是否存在
     */
    @Override
    public Boolean isDicExist() {
        return CommonUtils.isFileExist(Paths.get(utilboxFileRootPath, "tanyoudbbox", "dic", "dic.txt").toString());
    }

    /**
     * 接口实现类：探优数据库校对该字段,在结果目录utilbox/tanyoudbbox/out里面生成sql语句
     *
     * @param field 探优数据库字段
     */
    @Override
    public String modifyTanyoudbField(String field) throws IOException {
        String tanyoudbPath = Paths.get(utilboxFileRootPath, "tanyoudbbox", "db", "tanyoudb.db").toString();
        String dicPath = Paths.get(utilboxFileRootPath, "tanyoudbbox", "dic", "dic.txt").toString();
        System.out.println("Python脚本正则运行中");
        //调用python脚本
        String pythonReturn = "";
        if (!field.equals("fancha")) {
            pythonReturn = PythonUtils.modifyTanyoudb(tanyoudbPath, field, dicPath);
        }else {
            pythonReturn = PythonUtils.modifyTanyoudb(tanyoudbPath, dicPath);
        }
        if (pythonReturn.isEmpty()) return "";

        //把生成的SQL和没有找到的cve_notfound.txt复制到静态资源utilbox/tanyoudbbox/SQL/时间 路径。
        //获取当前时间，并用把时间加入路径
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String datePath = dateFormat.format(new Date());

        File sqlDirPath = new File(Paths.get(utilboxFileRootPath, "tanyoudbbox", "sql", datePath).toString());
        if (!sqlDirPath.exists()) sqlDirPath.mkdirs();

        //复制脚本可能成功生成的结果到file explorer相应目录
        for (String filename : TanyoudbboxReportFilenameEnum.getAllFilename()) {
            if (CommonUtils.isFileExist(filename)) {
                String fileAbsPath = CommonUtils.resolveFilePath(filename);
                String fileTargetPath = sqlDirPath + File.separator + filename;
                CommonUtils.copyFile(fileAbsPath, fileTargetPath);
            }
        }
        //返回结果
        return pythonReturn;
    }


}
