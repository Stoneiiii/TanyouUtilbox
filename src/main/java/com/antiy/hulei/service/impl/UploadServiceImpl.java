package com.antiy.hulei.service.impl;

import com.antiy.hulei.service.UploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class UploadServiceImpl implements UploadService {

    // 映射目录
    @Value("${file.uploadFolder}")
    private String uploadFolder;
    // 资源的访问路径
    @Value("${file.staticPatterPath}")
    private String staticPatterPath;

    @Override
    public String getUploadFileUrl(MultipartFile multipartFile, String dir) {
        try {
            // 1:真实的文件名称
            String originalFilename = multipartFile.getOriginalFilename(); // 例如上传的文件aa.xxx
            // 2:截取后的文件名称 .jpg
            assert originalFilename != null;
            String fileSuffix = originalFilename.substring(originalFilename.lastIndexOf(".")); // 得到.xxx
            // 3:生成唯一的文件名称
            String newFileName = UUID.randomUUID().toString() + fileSuffix;  // 随机生成如：dfasf42432.xxx
            // 4:日期作为目录隔离文件
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            String datePath = dateFormat.format(new Date()); // 日期目录：2023/11/11
            // 5:最终文件的上传目录
            File targetFile = new File(uploadFolder + dir, datePath); // 生成的最终目录; /tmp/avatar/2021/11/21
            // 6：如果dirFile不存在，则创建
            if (!targetFile.exists()) targetFile.mkdirs();
            // 7: 指定文件上传后完整的文件名
            File dirFileName = new File(targetFile, newFileName); // 文件在服务器的最终路径是：/tmp/avatar/2021/11/21/dfasf42432.jpg
            // 8：文件上传
            multipartFile.transferTo(dirFileName);
            // 9：可访问的路径 http://localhost:xxxx/avatar/2021/11/21/dfasf42432.xxx
            String fileName = dir + "/" + datePath + "/" + newFileName;
            return staticPatterPath+ fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return "fail";
        }
    }

    @Override
    public String getUploadFileABSPath(MultipartFile multipartFile, String dir) {
        try {
            // 1:真实的文件名称
            String originalFilename = multipartFile.getOriginalFilename(); // 例如上传的文件aa.xxx
            // 2:截取后的文件名称 .xxx
            assert originalFilename != null;
            String fileSuffix = originalFilename.substring(originalFilename.lastIndexOf(".")); // 得到.xxx
            // 3:生成唯一的文件名称
            String newFileName = UUID.randomUUID().toString() + fileSuffix;  // 随机生成如：dfasf42432.xxx
            // 5:最终文件的上传目录
            File targetFile = new File(uploadFolder + dir); // 生成的最终目录; /tmp/avatar/
            // 6：如果dirFile不存在，则创建
            if (!targetFile.exists()) targetFile.mkdirs();
            // 7: 指定文件上传后完整的文件名
            File dirFileName = new File(targetFile, newFileName); // 文件在服务器的最终路径是：/tmp/avatar/dfasf42432.xxx
            // 8：文件上传
            multipartFile.transferTo(dirFileName);
            return uploadFolder + dir + '/' + newFileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
