package com.antiy.hulei.util;

import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;

public class PythonUtils {


    public PythonUtils() {
        //python环境路径
        String pythonEnv = "venv/bin/python3.8";

        //初始化执行CMD环境
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);

        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(streamHandler);
    }

//    public static String
}
