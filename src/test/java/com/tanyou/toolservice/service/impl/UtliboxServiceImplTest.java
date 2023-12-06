package com.tanyou.toolservice.service.impl;

import com.tanyou.toolservice.service.UploadService;
import com.tanyou.toolservice.service.UtilboxService;
import com.tanyou.toolservice.util.CommonUtils;
import com.tanyou.toolservice.util.PythonUtils;
import com.tanyou.toolservice.util.Unzip;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@AutoConfigureMockMvc
@SpringBootTest
class UtliboxServiceImplTest {

    @InjectMocks
    private UtliboxServiceImpl utilboxService;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    ResourceLoader resourceLoader;


    @Mock
    UploadService uploadService;


    private static MockedStatic<PythonUtils> pythonUtilsStatic;
    private static MockedStatic<Unzip> unzipStatic;
    private static MockedStatic<CommonUtils> CommonUtilsStatic;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        //mock静态方法
        pythonUtilsStatic = Mockito.mockStatic(PythonUtils.class);
        unzipStatic = Mockito.mockStatic(Unzip.class);
        CommonUtilsStatic = Mockito.mockStatic(CommonUtils.class);
    }

    @AfterEach
    void after(){
        //用完关闭
        pythonUtilsStatic.close();
        unzipStatic.close();
        CommonUtilsStatic.close();
    }


    @Test
    void getPathService() {
        String dir = "/media/stone/data/myproject/python/modify_cve_cnvd/cnnvd";
        String[] s = utilboxService.getPathService(dir);
        assertEquals(0, s.length);
    }

    @Test
    void getPatchCmd() {
        String cveName = "1";
        String url = "https://android.googlesource.com/platform/frameworks/av/+/acb81624b4f50fed52cb1b3829809ee2f7377093";
        String androidVersion = "13";
        String res = "mkdir -p /data/extraPatch/1/frameworks/av/ && let b=b=$(find /data/extraPatch/1/frameworks/av/ -name \"*.patch\" | wc -l)+1 && cd /data/androidSource/getPatch/android-13.0.0_r1/frameworks/av/ && git checkout acb81624b4f50fed52cb1b3829809ee2f7377093 && git format-patch -1 acb81624b4f50fed52cb1b3829809ee2f7377093 && for name in *.patch; do mv \"$name\" \"/data/extraPatch/1/frameworks/av/${name/0001/000$b}\"; done && gst && echo -e \"\\033[43;32m 1提取成功！！\\033[0m\"";

        String actualRes = utilboxService.getPatchCmd(cveName, url, androidVersion);
        assertEquals(res, actualRes);
    }

    @Test
    void parseHtml() {
        //mock上传的文件
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "filename.txt", "text/html", "File content".getBytes());
        String dir = "null";
        String type = "Framework";
        //mock方法返回的结果
        doReturn("res").when(uploadService).getUploadFileABSPath(any(), any());
        //mock静态方法返回的结果
        pythonUtilsStatic.when(()->PythonUtils.getPatchInfo(any(),any())).thenReturn("1::2::3");

        Map<String, List<String>> actualRes = utilboxService.parseHtml(mockMultipartFile, dir, type);
        System.out.println(actualRes);
    }

    @Test
    void genCnnvdDic() throws IOException, NoSuchMethodException {
        //mock方法返回的结果
        doReturn("ok").when(uploadService).getUploadFileABSPath(any(), any());
        //mock方法返回的结果
        unzipStatic.when(()->Unzip.zipDecompress(any(),any())).thenAnswer(invocation -> null);
        pythonUtilsStatic.when(()->PythonUtils.cnnvdDataProcess(any(),any())).thenReturn(Boolean.TRUE);
        //mock上传的文件
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "filename.txt", "text/html", "File content".getBytes());

        Boolean res = utilboxService.genCnnvdDic(mockMultipartFile);
        assertEquals(res, Boolean.TRUE);
    }

    @Test
    void storeTanyoudb() throws IOException {
        //mock上传的文件
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "filename.txt", "text/html", "File content".getBytes());
        //mock方法返回的结果
        CommonUtilsStatic.when(()->CommonUtils.copyFile(any(),any())).thenAnswer(invocation -> null);
        CommonUtilsStatic.when(()->CommonUtils.getFileMD5(any())).thenReturn("ok");

        String res = utilboxService.storeTanyoudb(mockMultipartFile);
        assertEquals(res, "ok");
    }

    @Test
    void isTanyoudbExist() throws IOException {
        //mock方法返回的结果
        CommonUtilsStatic.when(()->CommonUtils.isFileExist(any())).thenReturn(Boolean.TRUE);

        Boolean tanyoudbExist = utilboxService.isTanyoudbExist();
        assertEquals(tanyoudbExist, Boolean.TRUE);
    }

    @Test
    void isDicExist() {
        //mock方法返回的结果
        CommonUtilsStatic.when(()->CommonUtils.isFileExist(any())).thenReturn(Boolean.TRUE);

        Boolean dicExist= utilboxService.isDicExist();
        assertEquals(dicExist, Boolean.TRUE);
    }

    @Test
    void modifyTanyoudbField() throws IOException {
        //mock方法返回的结果
        pythonUtilsStatic.when(()->PythonUtils.modifyTanyoudb(any(),any())).thenReturn("ok1");
        pythonUtilsStatic.when(()->PythonUtils.modifyTanyoudb(any(),any(),any())).thenReturn("ok2");

        String res1 = utilboxService.modifyTanyoudbField("fancha");
        assertEquals(res1, "ok1");
        String res2 = utilboxService.modifyTanyoudbField("cnnvd_no");
        assertEquals(res2, "ok2");
    }
}