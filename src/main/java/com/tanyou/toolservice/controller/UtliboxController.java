package com.tanyou.toolservice.controller;

import com.tanyou.toolservice.service.UtilboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/utlibox")
public class UtliboxController {

    @Autowired
    UtilboxService utilboxService;

    /**
     * 传入cve编号和cve编号详细信息的url后，生成在服务器中提取官方补丁的一句话CMD命令
     * 例如 cveName：CVE-2023-21238
     * url:<a href="https://android.googlesource.com/platform/frameworks/base/+/91bfcbbd87886049778142618a655352b16cd911">...</a>
     * @param cveName cve编号
     * @param url cve编号详细信息的url
     * @return String：在服务器中提取官方补丁的一句话CMD命令
     */
    @PostMapping("/getpatch/gencmd")
    public String getCveCmd(@RequestParam("cveName") String cveName,
                                  @RequestParam("url") String url) {
        return utilboxService.getPatchCmd(cveName, url);
    }


    /**
     * 传入谷歌每月发布Android漏洞页面下载后的html文件，
     * 生成上传文件该月份所有补丁的在服务器中提取官方补丁的一句话CMD命令
     * html获取方式为web页面右键->另存为
     * @param multipartFile 谷歌每月发布Android漏洞页面下载后的html文件
     * @param request 上传请求中获得的文件信息：想要提取的补丁类型
     * @return ModelAndView: 其中包含cve编号和对应生成的一句话CMD命令的字典Map<String, String>
     */
    @PostMapping("/getpatch/htmlparse")
    @ResponseBody
    public ModelAndView getAllCveCmd(@RequestParam("file") MultipartFile multipartFile,
                                      HttpServletRequest request) {
        //上传为空的判断
        if (multipartFile.isEmpty()) {
            System.out.println("上传为空");
            return null;//这里null会粗发ajax中的弹窗
        }

        //获得上传文件的信息
        String dir = "cveHtml"; //存放路径
        String type = request.getParameter("type"); //Android漏洞的类型
        Map<String, List<String>> cvenameUrlMap = utilboxService.parseHtml(multipartFile, dir, type);

        //生成Mam<cveName, 一句话CMD>
        Map<String, String> returnValue = new LinkedHashMap<>();
        for (String cveName : cvenameUrlMap.keySet()) {
            int count = 0; //计数url个数：也就是一个cveName 对应多个补丁
            for(String url : cvenameUrlMap.get(cveName)){
                // 获得一句话CMD
                String cmd = utilboxService.getPatchCmd(cveName, url);
                // 如果一个cveName对应了多个补丁，在cveName后追加一个序号：CVE-2023-21244_1
                if (count != 0) {
                    returnValue.put(cveName + '_'+ count, cmd);
                }else {
                    returnValue.put(cveName, cmd);
                }
                count++;
            }
        }

        //把Map传入ModelAndView中返回
        ModelAndView mv = new ModelAndView();
        mv.addObject("Map", returnValue);
        mv.setViewName("utilbox/autoextracve::cvetable");
        return mv;
    }


    /**
     * 上传CNNVD下载的xml压缩包，生成dic文件，放在相应目录中。
     * @param multipartFile xml的压缩包
     * @return String: 返回信息
     * @throws IOException
     */
    @PostMapping("/tanyoudb/gendic")
    public String uploadCnnvdXml(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        //上传为空的判断
        if (multipartFile.isEmpty()) {
            System.out.println("上传为空");
            return null;//这里null会粗发ajax中的弹窗
        }
        // 获得文件后缀
        String fileType = Objects.requireNonNull(multipartFile.getOriginalFilename()).
                substring(multipartFile.getOriginalFilename().lastIndexOf(".") + 1);
        //判断文件后缀是否为支持的压缩包格式
        if (!fileType.equals("zip")) {
            return "上传文件类型有误，请重新上传！";
        }

        //解析压缩包
        if (utilboxService.genCnnvdDic(multipartFile)) {
            return "生成字典成功！";
        }else {
            return "生成字典失败！";
        }
    }


    /**
     * 上传探优数据库的.db文件，保存到静态文件目录，供后续使用。
     * @param multipartFile 探优数据库文件
     * @return 数据库上传结果
     */
    @PostMapping("/tanyoudb/uploaddb")
    public String uploadTanyoudb(@RequestParam("dbfile") MultipartFile multipartFile) throws IOException {
        //上传为空的判断
        if (multipartFile.isEmpty()) {
            System.out.println("上传为空");
            return null; //这里null会粗发ajax中的弹窗
        }
        // 获得文件后缀
        String fileType = Objects.requireNonNull(multipartFile.getOriginalFilename()).
                substring(multipartFile.getOriginalFilename().lastIndexOf(".") + 1);
        //判断文件后缀是否为支持的压缩包格式
        if (!fileType.equals("db")) {
            return "上传文件类型有误，请重新上传！";
        }

        //保存上传db到静态目录
        String md5 = utilboxService.storeTanyoudb(multipartFile);

        return "上传数据库成功！<br>MD5:" + md5;
    }


    /**
     * 接受网页传递的tanyoudb字段名的值，调用python脚本，校对数据库中该字段。返回sql文件
     * @param filedName 探优数据库中字段名
     * @return 结果
     */
    @PostMapping("/tanyoudb/modifydb")
    public String modifyTanyoudb(@RequestParam("fieldName") String filedName) throws IOException {
        //检查cnnvd字典是否存在。
        if (!utilboxService.isDicExist()) {
            return "CNNVD字典文件不存在，请先上传！";
        }

        //检查探优数据是否有文件。
        if (!utilboxService.isTanyoudbExist()) {
            return "探优数据库文件不存在，请先上传！";
        }

        //校对数据库该字段,并返回结果报告
        return utilboxService.modifyTanyoudbField(filedName).replace("\n", "<br>");
    }


}
