package com.clps.utils.text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * 自动识别文件编码格式
 * UTF-8 UTF-16 GBK ISO-8859-1 ANSI UTF-8(WITH BOM)
 *
 * @author
 */
public class FileEncodeHelper {

    private static int BYTE_SIZE = 8;
    public static String CODE_UTF8 = "UTF-8";
    public static String CODE_UTF8_BOM = "UTF-8_BOM";
    public static String CODE_GBK = "GBK";

    /**
     * 通过文件全名称获取编码集名称
     *
     * @param fullFileName
     * @param ignoreBom
     * @return
     * @throws Exception
     */
    public static String getEncode(String fullFileName, boolean ignoreBom) throws Exception {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fullFileName));
        return getEncode(bis, ignoreBom);
    }

    /**
     * 通过文件缓存流获取编码集名称，文件流必须为未曾
     *
     * @param bis
     * @param ignoreBom 是否忽略utf-8 bom
     * @return
     * @throws Exception
     */
    public static String getEncode(BufferedInputStream bis, boolean ignoreBom) throws Exception {
        bis.mark(0);

        String encodeType = "未识别";
        byte[] head = new byte[3];
        bis.read(head);
        if (head[0] == -1 && head[1] == -2) {
            encodeType = "UTF-16";
        } else if (head[0] == -2 && head[1] == -1) {
            encodeType = "Unicode";
        } else if (head[0] == -17 && head[1] == -69 && head[2] == -65) { // 带BOM
            if (ignoreBom) {
                encodeType = CODE_UTF8;
            } else {
                encodeType = CODE_UTF8_BOM;
            }
        } else if ("Unicode".equals(encodeType)) {
            encodeType = "UTF-16";
        } else if (isUTF8(bis)) {
            encodeType = CODE_UTF8;
        } else {
            encodeType = CODE_GBK;
        }
        return encodeType;
    }

    /**
     * 是否是无BOM的UTF8格式，不判断常规场景，只区分无BOM UTF8和GBK
     *
     * @param bis
     * @return
     */
    private static boolean isUTF8(BufferedInputStream bis) throws Exception {
        bis.reset();

        // 读取第一个字节
        int code = bis.read();
        do {
            BitSet bitSet = convert2BitSet(code);
            // 判断是否为单字节
            if (bitSet.get(0)) {// 多字节时，再读取N个字节
                if (!checkMultiByte(bis, bitSet)) {// 未检测通过,直接返回
                    return false;
                }
            } else {
                // 单字节时什么都不用做，再次读取字节
            }
            code = bis.read();
        } while (code != -1);
        return true;
    }

    /**
     * 检测多字节，判断是否为utf8，已经读取了一个字节
     *
     * @param bis
     * @param bitSet
     * @return
     */
    private static boolean checkMultiByte(BufferedInputStream bis, BitSet bitSet) throws Exception {
        int count = getCountOfSequential(bitSet);
        byte[] bytes = new byte[count - 1];// 已经读取了一个字节，不能再读取
        bis.read(bytes);
        for (byte b : bytes) {
            if (!checkUtf8Byte(b)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检测单字节，判断是否为utf8
     *
     * @param b
     * @return
     */
    private static boolean checkUtf8Byte(byte b) throws Exception {
        BitSet bitSet = convert2BitSet(b);
        return bitSet.get(0) && !bitSet.get(1);
    }

    /**
     * 检测bitSet中从开始有多少个连续的1
     *
     * @param bitSet
     * @return
     */
    private static int getCountOfSequential(BitSet bitSet) {
        int count = 0;
        for (int i = 0; i < BYTE_SIZE; i++) {
            if (bitSet.get(i)) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    /**
     * 将整形转为BitSet
     *
     * @param code
     * @return
     */
    private static BitSet convert2BitSet(int code) {
        BitSet bitSet = new BitSet(BYTE_SIZE);

        for (int i = 0; i < BYTE_SIZE; i++) {
            int tmp3 = code >> (BYTE_SIZE - i - 1);
            int tmp2 = 0x1 & tmp3;
            if (tmp2 == 1) {
                bitSet.set(i);
            }
        }
        return bitSet;
    }

    /**
     * 将一指定编码的文件转换为另一编码的文件
     *
     * @param oldFullFileName
     * @param oldCharsetName
     * @param newFullFileName
     * @param newCharsetName
     */
    public static void convert(String oldFullFileName, String oldCharsetName, String newFullFileName,
            String newCharsetName) throws Exception {
        StringBuffer content = new StringBuffer();

        BufferedReader bin = new BufferedReader(
                new InputStreamReader(new FileInputStream(oldFullFileName), oldCharsetName));
        String line;
        while ((line = bin.readLine()) != null) {
            content.append(line);
            content.append(System.getProperty("line.separator"));
        }
        newFullFileName = newFullFileName.replace("\\", "/");
        File dir = new File(newFullFileName.substring(0, newFullFileName.lastIndexOf("/")));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Writer out = new OutputStreamWriter(new FileOutputStream(newFullFileName), newCharsetName);
        out.write(content.toString());
    }

    /**
     * 将指定的文件以编码方式读取出来
     *
     * @param fileName
     * @param ignoreLineMarkers
     * @return
     */
    public static StringBuilder readFileToString(String fileName, String... ignoreLineMarkers) throws Exception {

        StringBuilder content = new StringBuilder();
        String oldCharsetName = getEncode(fileName, true);
        BufferedReader bin = null;
        if ("UTF-8".equals(oldCharsetName) || "UTF-8_BOM".equals(oldCharsetName)) {
            bin = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
        } else if ("GBK".equals(oldCharsetName)) {
            bin = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "GBK"));
        } else {
            bin = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-16"));
        }

        String line;
        String commentStr = null;
        if (ignoreLineMarkers != null && ignoreLineMarkers.length > 0) {
            commentStr = ignoreLineMarkers[0];
        }
        while ((line = bin.readLine()) != null) {
            if (commentStr != null) {
                if (!line.trim().startsWith(commentStr)) {
                    content.append(line);
                    content.append(System.getProperty("line.separator"));
                }
            } else {
                content.append(line);
                content.append(System.getProperty("line.separator"));
            }

        }
        bin.close();
        return content;
    }

    /**
     * 将指定的文件以编码方式读取出来
     *
     * @param fileName          可以是本地路径或者http路径
     * @param ignoreLineMarkers
     * @return
     */
    public static List<String> readFileToList(String fileName, String... ignoreLineMarkers) throws Exception {
        // File ss =new File("qwer.txt");
        // if(!ss.exists()){
        // ss.createNewFile();
        // }
        // System.out.println(ss.exists());
        List<String> data = new ArrayList<>();
        String oldCharsetName = "UTF-8";
        if (!fileName.toLowerCase().trim().startsWith("http://")){
            oldCharsetName= getEncode(fileName, true);
        } 
        BufferedReader bin = null;
        HttpURLConnection connection = null;
        String charset = "UTF-16";
        if ("UTF-8".equals(oldCharsetName) || "UTF-8_BOM".equals(oldCharsetName)) {
            charset = "UTF-8";
        } else if ("GBK".equals(oldCharsetName)) {
            charset = "GBK";
        } else {

        }
        if (fileName.toLowerCase().trim().startsWith("http://")) { // http路径的文件
            try {
                URL url = new URL(fileName);
                // 通过远程url连接对象打开一个连接，强转成httpURLConnection类
                connection = (HttpURLConnection) url.openConnection();
                // 设置连接方式：get
                connection.setRequestMethod("GET");
                // 设置连接主机服务器的超时时间：15000毫秒
                connection.setConnectTimeout(150000000);
                // 设置读取远程返回的数据时间：60000毫秒
                connection.setReadTimeout(600000000);
                // 发送请求
                connection.connect();
                // 通过connection连接，获取输入流
                if (connection.getResponseCode() == 200) {
                    // 封装输入流is，并指定字符集
                    bin = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

                }
            } catch (Exception ex) {
                throw new Exception("读取" + fileName + "远程文件失败！");
            }
        } else { // 本地路径的文件
            bin = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), charset));
        }

        String line;
        String commentStr = null;
        if (ignoreLineMarkers != null && ignoreLineMarkers.length > 0) {
            commentStr = ignoreLineMarkers[0];
        }
        while ((line = bin.readLine()) != null) {
            if (commentStr != null) {
                if (!line.trim().startsWith(commentStr)) {
                    data.add(line);
                }
            } else {
                data.add(line);
            }

        }
        bin.close();
        if (connection != null) {
            connection.disconnect();
        }
        return data;
    }
}