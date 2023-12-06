package com.tanyou.toolservice.controller;

import com.tanyou.toolservice.service.UtilboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.ContentResultMatchers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class UtliboxControllerTest {

    @InjectMocks
    @Autowired
    private UtliboxController utliboxController;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    UtilboxService utilboxService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("controller:提取cve一句话CMD")
    void getCveCmd() throws Exception {
        //预计结果
        ContentResultMatchers content = MockMvcResultMatchers.content();
        //预计本次调用时成功的：状态200
        String res = "ok";
        ResultMatcher resultMatcher = content.string(res);
        //mock方法返回的结果
        doReturn(res).when(utilboxService).getPatchCmd(any(),any(),any());

        //请求测试
        MvcResult mvcResult = mockMvc.perform(post("/utlibox/getpatch/gencmd?cveName=1&url=https%3A%2F%2Fandroid.googlesource.com%2Fplatform%2Fframeworks%2Fbase%2F%2B%2F44aeef1b82ecf21187d4903c9e3666a118bdeaf3&androidVersion=13")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))    //设定Content-Type
                .andExpect(status().isOk()) //预期结果1：200
                .andExpect(resultMatcher)   //预期结果2：body的内容一致
                .andReturn();   //返回结果
        //打印结果
        System.out.println(mvcResult.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("controller:上传html页面")
    void getAllCveCmd() throws Exception {
        //mock上传的文件
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "filename.txt", "text/html", "File content".getBytes());
        MvcResult mvcResult = mockMvc.perform(multipart("/utlibox/getpatch/htmlparse")
                        .file(mockMultipartFile)    //加如mock的文件
                        .param("type", "Framework") //加入额外的参数
                        .contentType(MediaType.MULTIPART_FORM_DATA))    //设定Content-Type
                        .andExpect(status().isOk()) //预期结果：200
                .andReturn();
    }

    @Test
    void uploadCnnvdXml() throws Exception {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "filename.txt", "text/html", "File content".getBytes());
        MvcResult mvcResult = mockMvc.perform(multipart("/utlibox/tanyoudb/gendic")
                        .file(mockMultipartFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();
        //打印结果
        String res = mvcResult.getResponse().getContentAsString();
        System.out.println(res);
        assertEquals(res, "上传文件类型有误，请重新上传！");

        //mock方法返回的结果
        doReturn(Boolean.TRUE).when(utilboxService).genCnnvdDic(any());
        MockMultipartFile mockMultipartFile2 = new MockMultipartFile("file", "filename.zip", "text/html", "File content".getBytes());
        MvcResult mvcResult2 = mockMvc.perform(multipart("/utlibox/tanyoudb/gendic")
                        .file(mockMultipartFile2)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();
        //打印结果
        String res2 = mvcResult2.getResponse().getContentAsString();
        System.out.println(res2);
        assertEquals(res2, "生成字典成功！");
    }

    @Test
    void uploadTanyoudb() throws Exception {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("dbfile", "filename.txt", "text/html", "File content".getBytes());
        MvcResult mvcResult = mockMvc.perform(multipart("/utlibox/tanyoudb/uploaddb")
                        .file(mockMultipartFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();
        //打印结果
        String res = mvcResult.getResponse().getContentAsString();
        System.out.println(res);
        assertEquals(res, "上传文件类型有误，请重新上传！");

        //mock方法返回的结果
        doReturn("ok").when(utilboxService).storeTanyoudb(any());
        MockMultipartFile mockMultipartFile2 = new MockMultipartFile("dbfile", "filename.db", "text/html", "File content".getBytes());
        MvcResult mvcResult2 = mockMvc.perform(multipart("/utlibox/tanyoudb/uploaddb")
                        .file(mockMultipartFile2)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();
        //打印结果
        String res2 = mvcResult2.getResponse().getContentAsString();
        System.out.println(res2);
        assertEquals(res2, "上传数据库成功！<br>MD5:ok");
    }

    @Test
    void modifyTanyoudb() throws Exception {
        //mock方法返回的结果
        doReturn(Boolean.TRUE).when(utilboxService).isDicExist();
        doReturn(Boolean.TRUE).when(utilboxService).isTanyoudbExist();
        doReturn("ok").when(utilboxService).modifyTanyoudbField(any());
        //请求测试
        MvcResult mvcResult = mockMvc.perform(post("/utlibox/tanyoudb/modifydb?fieldName=cnnvd_no")
                        .contentType(MediaType.TEXT_PLAIN))    //设定Content-Type
                .andExpect(status().isOk()) //预期结果1：200
                .andReturn();   //返回结果
        //打印结果
        System.out.println(mvcResult.getResponse().getContentAsString());
    }
}