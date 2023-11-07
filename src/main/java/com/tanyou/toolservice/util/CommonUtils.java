package com.tanyou.toolservice.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

@Component
public class CommonUtils {


    /**
     * 返回资源目录下的文件的目录
     * @param filename 文件名
     * @return 文件路径
     */
    public static String resolveResFilePath(String filename) {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("classpath:" + filename);

        try {
            return resource.getFile().getPath();
//            InputStream inputStream = resource.getInputStream();
//            byte[] b = new byte[inputStream.available()];
//            inputStream.read(b);
//            return new String(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 返回静态资源文件夹的路径名，若不存在则创建。
     * @param dirname 静态资源文件夹
     * @return 路径名
     * @throws IOException None
     */
    public static String resolveResDirPath(String dirname) throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("classpath:" + dirname);
        //判断文件夹不存在，则创建
        if (!resource.exists()) {
            //获得根路径
            Resource resourceTmp = resourceLoader.getResource("classpath:");
            //拼接路径并创建
            String path = resourceTmp.getFile().getPath() + "/" + dirname;
            new File(path).mkdirs();
        }
        return resource.getFile().getPath();
    }


    /**
     * 返回文件MD5
     * @param filePath 文件路径
     * @return 文件MD5
     * @throws IOException None
     */
    public static String getFileMD5(String filePath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(filePath);
        String md5 = DigestUtils.md5Hex(fileInputStream);
        fileInputStream.close();
        return md5;
    }

    /**
     * 资源文件是否存在
     * @param filePath 文件路径
     * @return Boolean: 是否存在
     */
    public static Boolean isResFileExist(String filePath) {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("classpath:" + filePath);
        return resource.exists();
    }

    /**
     * 复制文件，并删除源文件
     * @param resPath 源文件
     * @param targetPath 目的文件
     */
    public static void copyFile(String resPath, String targetPath) throws IOException {
        File sourceFile = new File(resPath);
        File targetFile = new File(targetPath);
        //判断是否已经存在文件，有则删除
        if(targetFile.exists()) targetFile.delete();
        //copy and delete
        Files.copy(sourceFile.toPath(), targetFile.toPath());
        sourceFile.delete();
    }

    /**
     * 返回环境下文件实际的绝对路径：与pom.xml同目录下
     * @param filename 文件名
     * @return 绝对路径
     * @throws IOException None
     */
    public static String resolveFilePath(String filename) throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("file:" + filename);
        return resource.getFile().getPath();
    }

    /**
     * 环境文件是否存在
     * @param filePath 文件路径
     * @return Boolean: 是否存在
     */
    public static Boolean isFileExist(String filePath) {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("file:" + filePath);
        return resource.exists();
    }
}
