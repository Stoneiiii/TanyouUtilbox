package com.antiy.hulei.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface UtilboxService {

    //提取补丁：提取生成在服务器中提取官方补丁的命令
    /**
     * 接口类：接受CVE编号和对应的官方url信息，生成从安卓官网提取补丁命令
     * 配合fix_patch中getpatch环境中使用
     * 提取补丁存放目录：/data/extraPatch
     * @param cveName CVE编号
     * @param url 官方url信息
     * @return String: 一句话提取官方补丁CMD
     */
    public String getPatchCmd(String cveName, String url);

    //提取补丁：解析上传的html，获得cveName和url
    /**
     * 接口类：传入谷歌每月发布Android漏洞页面下载后的html文件，文件名，需要提取的Android漏洞类型
     * 调用python脚本解析文件内容，返回一个Map<cveName, 对应的一句话提取CMD（可能多条）>
     * @param multipartFile 传入谷歌每月发布Android漏洞页面下载后的html文件
     * @param dir 文件路径
     * @param type 需要提取的Android漏洞类型
     * @return 返回一个Map<cveName, 对应的一句话提取CMD（可能多条）>
     */
    public Map<String, List<String>> parseHtml(MultipartFile multipartFile, String dir, String type);

    //探优数据库：获得一个路径下所有文件的路径(暂时没用)
    /**
     * 接口类：返回路径下所有文件的路径
     * @param path 文件夹路径
     * @return String[]: 所有文件的路径
     */
    public String[] getPathService(String path) throws IOException;

    //探优数据库：获得cnnvd下载xml的压缩包，调用python脚本，生成字典文件路径
    /**
     * 接口类：接收cnnvd下载数据的压缩包，调用python脚本，生成字典文件路径
     * @param multipartFile cnnvd压缩包文件
     * @return 是否生成字典成功
     */
    public Boolean genCnnvdDic(MultipartFile multipartFile) throws IOException;

    //探优数据库：保存上传的探优数据库db文件到静态目录utilbox/tanyoudbbox/db
    /**
     * 接口类：保存上传的探优数据库db文件到静态目录utilbox/tanyoudbbox/db
     * @param multipartFile 探优数据库文件
     * @return String: 返回文件MD5
     */
    public String storeTanyoudb(MultipartFile multipartFile) throws IOException;

    //探优数据库：是否在资源文件目录存在探优db
    /**
     * 接口类：是否在资源文件目录存在探优db
     * @return Boolean:是否存在
     */
    public Boolean isTanyoudbExist() throws IOException;

    //探优数据库：是否在资源文件目录存在字典文件
    /**
     * 接口类：是否在资源文件目录存在字典文件
     * @return Boolean:是否存在
     */
    public Boolean isDicExist();

    //探优数据库：探优数据库校对该字段
    /**
     * 接口类：探优数据库校对该字段,在结果目录utilbox/tanyoudbbox/out里面生成sql语句
     * @param field 探优数据库字段
     */
    public String modifyTanyoudbField(String field) throws IOException;
}
