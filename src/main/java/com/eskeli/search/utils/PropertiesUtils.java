package com.eskeli.search.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Class Desc : 配置文件 .properties file 读取工具
 *
 * @author elisontl
 */
public class PropertiesUtils {

    private PropertiesUtils() {

    }

    private static class PropertiesUtilsHolder {
        private final static PropertiesUtils INSTANCE = new PropertiesUtils();
        private final static Properties PROPERTIES_INSTANCE = new Properties();
    }

    public static PropertiesUtils getInstance() {
        return PropertiesUtilsHolder.INSTANCE;
    }

    private static Properties getProperties() {
        return PropertiesUtilsHolder.PROPERTIES_INSTANCE;
    }

    /**
     * 加载指定配置文件
     *
     * @param propertyPath : String
     * @exception
     */
    public PropertiesUtils loadConfig(String propertyPath) {
        try {
            InputStream is = this.getClass().getResourceAsStream(propertyPath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            getProperties().load(br);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getInstance();
    }

    /**
     * 根据指定键get值
     *
     * @param key : String
     * @return
     */
    public String getProperty(String key) {
        return getProperties().getProperty(key);
    }

}
