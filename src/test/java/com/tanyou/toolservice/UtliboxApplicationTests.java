package com.tanyou.toolservice;

import com.tanyou.toolservice.service.UtilboxService;
import com.tanyou.toolservice.util.CommonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

import static com.tanyou.toolservice.util.CommonUtils.resolveResFilePath;

@SpringBootTest
class UtliboxApplicationTests {

    @Autowired
    UtilboxService utilboxService;


    @Autowired
    ResourceLoader resourceLoader;

    @Test
    void getPathServiceTest() throws IOException {


        String a = "/media/stone/data/myproject/python/modify_cve_cnvd/cnnvd";
        String[] s = utilboxService.getPathService(a);
        for(String t : s) {
            System.out.println(t);
        }

    }

    @Test
    void resolvePythonScriptPathTest() {
        System.out.println(resolveResFilePath("scripts/find_info_from_cnnvd.py"));
    }

    @Test
    void getPatchCmdTest() {
        String cveName = "CVE-2023-21145";
        String url = "https://android.googlesource.com/platform/frameworks/base/+/44aeef1b82ecf21187d4903c9e3666a118bdeaf3";
        System.out.println(utilboxService.getPatchCmd(cveName, url));
    }

    @Test
    void parseHtmlTest(){
        String res = "CVE-2023-21273::['https://android.googlesource.com/platform/packages/modules/Bluetooth/+/1e27ef69755a0735278a1c6af130c71a92b94e3f']\n" +
                "CVE-2023-20965::['https://android.googlesource.com/platform/packages/modules/Wifi/+/88a8a98934215f591605028e200b6eca8f7cc45a', 'https://android.googlesource.com/platform/packages/modules/Wifi/+/bd318b9772759546509f6fdb8648366099dd65ad', 'https://android.googlesource.com/platform/packages/modules/Wifi/+/0d3cb609b0851ea9e5745cc6101e57c2e5e739f2']\n" +
                "CVE-2023-21132::['https://android.googlesource.com/platform/packages/modules/Permission/+/0679e4f35055729be7276536fe45fe8ec18a0453']\n" +
                "CVE-2023-21133::['https://android.googlesource.com/platform/packages/modules/Permission/+/0679e4f35055729be7276536fe45fe8ec18a0453']\n" +
                "CVE-2023-21134::['https://android.googlesource.com/platform/packages/modules/Permission/+/0679e4f35055729be7276536fe45fe8ec18a0453']\n" +
                "CVE-2023-21140::['https://android.googlesource.com/platform/packages/modules/Permission/+/0679e4f35055729be7276536fe45fe8ec18a0453']\n" +
                "CVE-2023-21242::['https://android.googlesource.com/platform/packages/modules/Wifi/+/72e903f258b5040b8f492cf18edd124b5a1ac770']\n" +
                "CVE-2023-21275::['https://android.googlesource.com/platform/packages/apps/ManagedProvisioning/+/8277a2a946e617a7ea65056e4cedeb1fecf3a5f5']\n" +
                "CVE-2023-21271::['https://android.googlesource.com/platform/packages/modules/NeuralNetworks/+/e44e1064ccec2aa09fc66bd750d66919129ae6b4']\n" +
                "CVE-2023-21274::['https://android.googlesource.com/platform/packages/modules/NeuralNetworks/+/2bffd7f5e66dd0cf7e5668fb65c4f2b2e9f87cf7']\n" +
                "CVE-2023-21285::['https://android.googlesource.com/platform/frameworks/base/+/0c3b7ec3377e7fb645ec366be3be96bb1a252ca1']\n" +
                "CVE-2023-21268::['https://android.googlesource.com/platform/packages/providers/TelephonyProvider/+/ca4c9a19635119d95900793e7a41b820cd1d94d9']\n" +
                "CVE-2023-21290::['https://android.googlesource.com/platform/packages/providers/TelephonyProvider/+/ca4c9a19635119d95900793e7a41b820cd1d94d9']";
        String[] split = res.split("\n");
        for (String s : split) {
            String[] sp = s.split("::");
            System.out.println(sp[0]);
            for (String string : sp[1].split(",")) {
                System.out.println(string.replace("[","")
                        .replace("]","")
                        .replace("'",""));
            }
            System.out.println("-------------");

        }
//        String localHtmlPath = "/tmp/avatar/15b4f7d5-1c7a-43d0-96c8-b3c603bd90ca.html";
//        File file = new File(localHtmlPath);
//        Map<String, String> avatar = ubliboxService.parseHtml((MultipartFile) file, "avatar");
    }


    @Test
    void zipTest() throws IOException {
//        File zipFile = new File("/media/stone/data/myproject/python/modify_cve_cnvd/scripts_10.24.zip");

        Resource dicRootPath = resourceLoader.getResource("classpath:");
//        File unzipFile = new File(dicRootPath.getFile().getPath(), "tanyoudb");
        System.out.println(dicRootPath.getFile().getPath());

//        Unzip.zipDecompress(zipFile, unzipFile);
    }


    @Test
    void resolveResDirPathTest() throws IOException {
//        System.out.println(CommonUtils.resolveResDirPath("utilbox"));
//        System.out.println(CommonUtils.resolveResDirPath("utilbox/tanyoudbbox"));


//
//        ResourceLoader resourceLoader = new DefaultResourceLoader();
//        Resource resource = resourceLoader.getResource("classpath:" + "utilbox/tanyoudbbox/dic/dic.txt");
//        Resource resource1 = resourceLoader.getResource("classpath:utilbox");
//        System.out.println(resource.getFile().getPath());
//        System.out.println(resource1.getFile().getPath());

        System.out.println(CommonUtils.isFileExist("/tmp/utilbox/tanyoudbbox/db"));


//        try {
//            FileReader fr = new FileReader(a);
//            BufferedReader br = new BufferedReader(fr);
//
//            String line;
//            while ((line = br.readLine()) != null) {
//                System.out.println(line);
//            }
//
//            br.close();
//            fr.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println(file.getPath());
    }

}
