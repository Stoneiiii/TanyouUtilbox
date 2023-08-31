package com.antiy.hulei.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Value("${file.staticPatterPath}")
    private String staticPatterPath;
    @Value("${file.uploadFolder}")
    private String uploadFolder;

    // 配置文件上传的额外的静态资源配置
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //  registry.addResourceHandler("/资源的访问路径").addResourceLocations("映射目录");
        registry.addResourceHandler(staticPatterPath + "**").addResourceLocations("file:" + uploadFolder);
    }
}
