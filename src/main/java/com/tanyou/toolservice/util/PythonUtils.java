package com.tanyou.toolservice.util;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@Service
public class PythonUtils {

    public static String pythonEnv;

    @Value("${python.pythonEnv}")
    public void setPythonEnv(String pythonEnv) {
        PythonUtils.pythonEnv = pythonEnv;
    }

    public String getkey() {
        return pythonEnv;
    }

    /**
     * 调用get_patch_info.py脚本解析html文件，返回cveName和url的字符串
     * @param targetFilePath html文件
     * @param type 接续的漏洞类型
     * @return String cveName和url的字符串
     */
    public static String getPatchInfo(String targetFilePath, String type) {
        String pythonReturn = null;
        //生成CMD命令的python脚本路径
        String pythonFilePath = CommonUtils.resolveResFilePath("scripts/get_patch_info.py");
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
        if (pythonReturn.isEmpty()) {
            System.out.println("error:python脚本输出为空！");
            return null;
        }
        return pythonReturn;
    }

    /**
     * 调用cnnvd_data_process.py脚本，解析CNNVD文件，生成字典
     * @param dicDirPath 字典的路径
     * @param unzipFilePath 解压出来CNNVD文件的路径
     * @return Boolean 是否生成字典成功
     */
    public static Boolean cnnvdDataProcess(String dicDirPath, String unzipFilePath) {
        String pythonReturn = null;
        //生成CMD命令的python脚本路径
        String pythonFilePath = CommonUtils.resolveResFilePath("scripts/cnnvd_data_process.py");
        //构造python命令：python环境 + python脚本 + 传入参数
        String line = pythonEnv + " " + pythonFilePath +
                " --file_path " + unzipFilePath +
                " --dic_path " + dicDirPath + File.separator + "dic.txt";
        //解析成CMD命令
        CommandLine cmdLine = CommandLine.parse(line);
        //初始化CMD环境
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
        if (pythonReturn.isEmpty()) {
            System.out.println("error:python脚本输出为空！");
            return false;
        }
        return true;
    }

    /**
     * 调用modify_tanyoudb.py脚本，校对数据库，生成结果
     * @param tanyoudbPath 数据库路径
     * @param field 数据库字段名
     * @param dicPath 字典文件路径
     * @return String 结果
     */
    public static String modifyTanyoudb(String tanyoudbPath, String field, String dicPath) {
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
        if (pythonReturn.isEmpty()) {
            System.out.println("error:python脚本输出为空！");
            return "";
        }
        return pythonReturn;
    }


    public static String modifyTanyoudb(String tanyoudbPath, String dicPath) {
        String pythonReturn = null;
        //生成CMD命令的python脚本路径
        String pythonFilePath = CommonUtils.resolveResFilePath("scripts/modify_tanyoudb.py");
        //python脚本选择运行那个功能：option的值
        String option = "2";
        //python环境路径
        String pythonEnv = "venv/bin/python3.8";
        //构造python命令：python环境 + python脚本 + 传入参数
        String line = pythonEnv + " " + pythonFilePath
                + " " + option
                + " --db_path " + tanyoudbPath
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
        if (pythonReturn.isEmpty()) {
            System.out.println("error:python脚本输出为空！");
            return "";
        }
        return pythonReturn;
    }

}
