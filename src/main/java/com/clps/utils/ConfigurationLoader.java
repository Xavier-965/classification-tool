package com.clps.utils;

import com.clps.exception.NoConfigFile;
import com.clps.utils.print.Color;
import com.clps.utils.print.Printer;

import java.io.*;
import java.net.URL;
import java.util.Properties;
/**
 * @author Xavier
 * @date 2023/6/28
 */
public class ConfigurationLoader {
    private static Properties defaultProperties = null;

    private ConfigurationLoader() {
    }

    /**
     * 从jar包/文件系统/远程文件获取配置文件。<\br>
     * <ol>
     * <li>如果是从jar包获取配置，支持相对和绝对路径></li>
     * <li>如果是从文件系统获取配置，路径支持绝对路径和相对路径</li>
     * </ol>
     *
     * @param propertiesName 可以是文件系统文件/jar包内文件/http文件
     * @return Properites
     */
    public static Properties getProperties(String... propertiesName) throws NoConfigFile {

        if (propertiesName == null || propertiesName.length == 0) {
            Printer.println("请至少配置一个有效路径", Color.Red);
            throw new NoConfigFile();
        }

        Properties properties = new Properties();
        Properties tmp = new Properties();
        for (String fPath : propertiesName) {
            InputStream is = null;
            InputStreamReader isr = null;
            File file;

            try {
                if(fPath.trim().trim().toLowerCase().startsWith("http")){ // 读取http文件
                    Printer.println("从http获取配置文件：" + fPath, Color.Green);
                    URL url = new URL(fPath);
                    is = url.openStream();
                }else {
                    file = new File(fPath);
                    String sysSeparator = File.separator;
                    if (!file.exists()) { // 文件系统不存在指定路径，改成从jar包读取
                        Printer.println(file.getAbsolutePath() + "系统配置文件不存在，尝试从包内读取", Color.Red);
                        is = ConfigurationLoader.class.getResourceAsStream(fPath); // 按指定路径从jar包加载资源文件
                        if (is == null) { // 加载指定包路径下的资源
                            if (!fPath.startsWith(sysSeparator)) { // 改成相对路径再加载
                                fPath = "/" + fPath;
                            } else { // 改成绝对路径再加载
                                fPath = fPath.replaceFirst(sysSeparator, "");
                            }
                            is = ConfigurationLoader.class.getResourceAsStream(fPath);
                        }else{
                            Printer.println(file.getAbsolutePath() + "已从包内读取配置文件", Color.Blue);
                        }
                    } else { // 从文件系统读取文件
                        Printer.println("从系统加载配置文件：" + fPath, Color.Green);
                        is = new FileInputStream(file);
                    }
                }

                if (is != null) {
                    isr = new InputStreamReader(is, "UTF-8");
                    tmp.load(isr); //.load(is,"UTF-8");
                    for (String key : tmp.stringPropertyNames()) {
                        if (tmp.getProperty(key) != null) {
                            properties.put(key, tmp.getProperty(key).trim());
                        }
                    }
                }
            } catch (IOException e) {
                Printer.println("加载配置文件：" + fPath + " 失败，请检查包内或者文件系统是否存在此文件", Color.Red);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    Printer.println("资源关闭失败！", Color.Red);
                }

                if (isr != null) {
                    try {
                        isr.close();
                    } catch (IOException e) {
                        Printer.println("输入流关闭失败！", Color.Red);
                    }
                }
            }
        }
        return properties;
    }

    /**
     * 加载文件系统里默认的配置数据
     *
     * @return
     */
    public static Properties getDefaultProperties() throws NoConfigFile{
        if (defaultProperties == null) {
            defaultProperties = getProperties("default.properties");
        }
        return defaultProperties;

    }
}
