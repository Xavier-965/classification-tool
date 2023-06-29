package com.clps.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/**
 * 从文件系统或者从jar包加载文件
 *
 * @author Tony.yang
 */
public class ResourceFileLoader {
    private static Logger logger = LogManager.getLogger(ResourceFileLoader.class);

    private ResourceFileLoader() {
    }
    //    public static Map<String, Double> idfDictMap = new ConcurrentHashMap<>();
//    public static Set<String> businessDictSet = Collections.synchronizedSet(new HashSet());

    /**
     * 如果文件存在于文件系统，则只加载物理文件；如果文件不存在于文件系统，则尝试从jar加载文件
     *
     * @param path 路径。如果是包路径，自动将路径处理为以/开头的包路径；如果是文件系统路径，可以是相对路径或绝对路径
     * @return InputStream
     */

    public static InputStream getInputStream(String path) throws FileNotFoundException {
        File f = new File(path);
        InputStream ins = null;

        if (f.exists()) { // 物理文件存在
            ins = new FileInputStream(f);
        } else { // 尝试从包中加载
            if (!path.startsWith(File.pathSeparator)) {
                path = File.pathSeparator + path;
            }
            ins = ResourceFileLoader.class.getResourceAsStream(path);
        }
        return ins;
    }

    /**
     * Get a file's inputStream from a local or http server
     *
     * @param path File path to be obtained，
     *             If the file path starts with “http:”, then get the http server-side file inputStream，
     *             otherwise the local file inputStream is obtained.
     *             example: path=http://localhost:8080/file/test.txt
     */
    public static InputStream getInputstreamOnHttp(String path) throws IOException {
        // 输入流
        InputStream inputStream = null;

        if (path.length() > 5 && path.startsWith("http:")) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5000); //设置连接超时为5秒
            HttpClient client = new DefaultHttpClient(httpParams); // 生成一个http客户端发送请求对象
            HttpGet httpget1 = new HttpGet(path); //对查询页面get
            HttpResponse httpResponse1 = client.execute(httpget1); // 发送请求并等待响应
            // 判断网络连接是否成功
            String[] str = path.split("/");
            String filename = "";
            LinkedList<String> queue = new LinkedList(Arrays.asList(str));
            try {
                filename = queue.getLast();
            } catch (Exception e) {
//                        logger.error(System.currentTimeMillis() + "com.clps.utils.ResourceFileLoader<getInputstreamOnHttp>:获取资源文件名字失败");
            }

            System.out.println("状态码：" + httpResponse1.getStatusLine().getStatusCode());
            if (httpResponse1.getStatusLine().getStatusCode() != 200) {
                System.out.println("网络错误异常！!!!");
//                        logger.error("网络错误异常，无法加载" + filename + "资源文件！!!!");

            } else {
                System.out.println("网络连接成功，正在加载" + filename + "资源文件!!!");
//                        logger.info("网络连接成功，正在加载" + filename + "资源文件!!!");
            }
            httpget1.abort();
            HttpGet httpget2 = new HttpGet(path); //对下载链接get实现下载

            HttpResponse httpResponse2 = client.execute(httpget2);
            HttpEntity entity = httpResponse2.getEntity(); // 获取响应里面的内容
            inputStream = entity.getContent(); // 得到服务气端发回的响应的内容（都在一个流里面）
        } else {
            inputStream = ResourceFileLoader.getInputStream(path);
        }

        return inputStream;
    }

    /**
     * This method loads the file into a java.util.Set and returns this java.util.Set
     *
     * @param path Dictionary file path to be loaded
     */
    public static Set<String> loadFileToSet(String path) throws IOException {
        Set<String> set = new HashSet<>();
        BufferedReader bufr = null;
        InputStream inputStream = null;
        inputStream = getInputstreamOnHttp(path);
        bufr = new BufferedReader(new InputStreamReader(inputStream));
        if (set.size() != 0) set.clear();
        String line = null;
        while ((line = bufr.readLine()) != null) {
            set.add(line.trim());
        }
        if (bufr != null) {
            try {
                bufr.close();
            } catch (IOException bufrE) {
//                    logger.info("bufr read Exception", bufrE.fillInStackTrace());
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException inputStreamE) {
//                    logger.info("inputStream close Exception", inputStreamE.fillInStackTrace());
            }
        }

        return set;
    }

    public static List<String> loadFileToList(String path) throws IOException {
        List<String> set = new ArrayList<>();
        BufferedReader bufr = null;
        InputStream inputStream = null;
        inputStream = getInputstreamOnHttp(path);
        bufr = new BufferedReader(new InputStreamReader(inputStream));
        if (set.size() != 0) set.clear();
        String line = null;
        while ((line = bufr.readLine()) != null) {
            set.add(line.trim());
        }
        if (bufr != null) {
            try {
                bufr.close();
            } catch (IOException bufrE) {
//                    logger.info("bufr read Exception", bufrE.fillInStackTrace());
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException inputStreamE) {
//                    logger.info("inputStream close Exception", inputStreamE.fillInStackTrace());
            }
        }

        return set;
    }
    /**
     * This method loads a file of the form <K, V> into a java.util.Map and returns this java.util.Map.
     *
     * @param path         Dictionary file path to be loaded
     * @param isOpenRemark If true, open the comment function of the file
     *                     If it is false or null, the comment function of the file is not turned on.
     */
    public static Map loadFileToMap(String path, boolean... isOpenRemark) throws IOException {

        Map map = new ConcurrentHashMap<>();
        BufferedReader bufr = null;
        InputStream inputStream = null;
        inputStream = getInputstreamOnHttp(path);
        bufr = new BufferedReader(new InputStreamReader(inputStream));
        if (map.size() != 0) map.clear();
        String line = null;
        while ((line = bufr.readLine()) != null) {
            if (isOpenRemark != null && isOpenRemark.length > 0 && isOpenRemark[0]) {
                if (line.trim().startsWith("#")) continue;

                String[] kv = line.trim().split(" ");
                map.put(kv[0], kv[1]);
            } else {
                String[] kv = line.trim().split(" ");
                map.put(kv[0], kv[1]);
            }
        }
        if (bufr != null) {
            try {
                bufr.close();
            } catch (IOException bufre) {
//                    logger.info("bufr read Exception", bufre.fillInStackTrace());
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException inputStreamE) {
//                    logger.info("inputStream close Exception", inputStreamE.fillInStackTrace());
            }
        }

        return map;
    }

    /**
     * 将同义词加载到map中，每个词都与一个同义词列表对应
     *
     * @param container    ConcurrentHashMap类型的并发容器。线程安全
     * @param path         http文件路径
     * @param spliter      分隔符
     * @param isOpenRemark 是否忽略#注释行。默认为true
     */
    public static void loadFileToMapDetail(ConcurrentHashMap<String, List<String>> container, String path,
                                           String spliter,
                                           boolean... isOpenRemark) {
        BufferedReader bufr = null;
        InputStream inputStream = null;
        try {
            inputStream = getInputstreamOnHttp(path);
            bufr = new BufferedReader(new InputStreamReader(inputStream));
            if (container.size() != 0) container.clear();
            String line = null;

            while ((line = bufr.readLine()) != null) {
                line = line.trim();
                if ("".equals(line)) {
                    continue;
                }
                String[] kv = null;
                if (isOpenRemark != null && isOpenRemark.length > 0 && isOpenRemark[0] && line.startsWith("#")) {
                    continue;
                } else {
                    kv = line.split(spliter);
                }
                if (kv != null) {
//                    Set<String> set = new ConcurrentSkipListSet<>();

                    List<String> list = Arrays.stream(kv).map(e -> e.toLowerCase().trim()).collect(Collectors.toList());

                    list.forEach(e -> container.put(e, list));
                }
            }
        } catch (Exception e) {
            if (bufr != null) {
                try {
                    bufr.close();
                } catch (IOException bufre) {
//                    logger.info("bufr read Exception", bufre.fillInStackTrace());
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException inputStreamE) {
//                    logger.info("inputStream close Exception", inputStreamE.fillInStackTrace());
                }
            }
        }
    }

    /**
     * 将同义词加载到map中，多个同义词作为KEY,标准词作为Value
     *
     * @param container    ConcurrentHashMap类型的并发容器。线程安全
     * @param path         http文件路径
     * @param spliter      分隔符
     * @param isOpenRemark 是否忽略#注释行。默认为true
     */
    public static void loadFileToMapSynDict(Map<String, String> container, String path,
                                            String spliter,
                                            boolean... isOpenRemark) {
        BufferedReader bufr = null;
        InputStream inputStream = null;
        try {
            inputStream = getInputstreamOnHttp(path);
            bufr = new BufferedReader(new InputStreamReader(inputStream));
            if (!container.isEmpty()) container.clear();
            String line = null;

            while ((line = bufr.readLine()) != null) {
                line = line.trim().toLowerCase();
                if ("".equals(line)) {
                    continue;
                }
                String[] kv = null;
                if (isOpenRemark != null && isOpenRemark.length > 0 && isOpenRemark[0] && line.startsWith("#")) {
                    continue;
                } else {
                    kv = line.split(spliter);
                }
                if (kv.length > 1) { // 第一个词为标准词
                    for (int i = 0; i < kv.length; i++) {
                        container.put(kv[i], kv[0]);
                    }
                }

            }
        } catch (Exception e) {
            if (bufr != null) {
                try {
                    bufr.close();
                } catch (IOException bufre) {
//                    logger.info("bufr read Exception", bufre.fillInStackTrace());
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException inputStreamE) {
//                    logger.info("inputStream close Exception", inputStreamE.fillInStackTrace());
                }
            }
        }
    }

    /**
     * 将 K - V 形式的文件加载进 Map
     *
     * @param container    ConcurrentHashMap类型的并发容器。线程安全
     * @param path         http文件路径
     * @param spliter      分隔符
     * @param isOpenRemark 是否忽略#注释行。默认为true
     */
    public static void loadFileToMap(Map<String, String> container, String path,
                                     String spliter,
                                     boolean... isOpenRemark) {
        try (InputStream inputStream = getInputstreamOnHttp(path);
             BufferedReader bufr = new BufferedReader(new InputStreamReader(inputStream));
        ) {
            if (!container.isEmpty()) container.clear();
            String line = null;

            while ((line = bufr.readLine()) != null) {
                line = line.trim().toLowerCase();
                if ("".equals(line)) {
                    continue;
                }
                String[] kv = null;
                if (isOpenRemark != null && isOpenRemark.length > 0 && isOpenRemark[0] && line.startsWith("#")) {
                    continue;
                } else {
                    kv = line.split(spliter, 2);
                    container.put(kv[0].toLowerCase(), kv[1]);
                }
            }
        } catch (Exception e) {

        }
    }
}
