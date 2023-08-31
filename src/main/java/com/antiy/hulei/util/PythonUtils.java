package com.antiy.hulei.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class PythonUtils {



    public static String resolvePythonScriptPath(String name) {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("classpath:" + name);

        try {
            return resource.getFile().getPath();
//            InputStream inputStream = resource.getInputStream();
//            byte[] b = new byte[inputStream.available()];
//            inputStream.read(b);
//            return new String(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
