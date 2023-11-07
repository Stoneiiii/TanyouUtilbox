package com.tanyou.toolservice.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Unzip {

    /**
     * 解压缩ZIP文件
     * @param zipFile ZIP文件
     * @param destDir 目标路径
     */
    public static void zipDecompress(File zipFile, File destDir) {
        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile.toPath()))) {
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                File file = new File(destDir, entry.getName());
                System.out.println(file.getPath());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    File parent = file.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                entry = zis.getNextEntry();
            }
            System.out.println("解压完成！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
