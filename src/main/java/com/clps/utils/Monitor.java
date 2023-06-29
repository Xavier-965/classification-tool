package com.clps.utils;
import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * @author Xavier
 * @date 2023/3/9
 */
public class Monitor implements Runnable {
    private static CloseableHttpClient httpclient = HttpClients.createDefault();
    /*
     * 上次更改时间
     */
    private String last_modified;
    /*
     * 资源属性
     */
    private String eTags;
    /*
     * 请求地址
     */
    private String location;

    public Monitor(String location) {
        this.location = location;
        this.last_modified = null;
        this.eTags = null;
    }

    /**
     * 监控流程：
     * ①向词库服务器发送Head请求
     * ②从响应中获取Last-Modify、ETags字段值，判断是否变化
     * ③如果未变化，休眠1min，返回第①步
     * ④如果有变化，重新加载词典
     * ⑤休眠1min，返回第①步
     */

    public void run() {
        // 关闭非http文件更新的功能 ############
        if (!location.startsWith("http://") && !location.startsWith("https://")) {
            return;
        }
        // ##################################

        //超时设置
        RequestConfig rc = RequestConfig.custom().setConnectionRequestTimeout(10 * 1000)
                .setConnectTimeout(10 * 1000).setSocketTimeout(15 * 1000).build();

        HttpHead head = new HttpHead(location);
        head.setConfig(rc);

        //设置请求头
        if (last_modified != null) {
            head.setHeader("If-Modified-Since", last_modified);
        }
        if (eTags != null) {
            head.setHeader("If-None-Match", eTags);
        }

        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(head);
            //返回200 才做操作
            if (response.getStatusLine().getStatusCode() == 200) {
                Header lastHeader = response.getLastHeader("Last-Modified");
                Header eTag = response.getLastHeader("ETag");
                if ((lastHeader != null && !lastHeader.getValue().equalsIgnoreCase(last_modified))
                        || (eTag != null && !eTag.getValue().equalsIgnoreCase(eTags))) {
                    if (last_modified != null || eTags != null) {
                        // 远程词库有更新,需要重新加载词典，并修改last_modified,eTags
                        WordDictionary.getSingleton().reLoadDict(location);
                    }
                    last_modified = lastHeader == null ? null : lastHeader.getValue();
                    eTags = eTag == null ? null : eTag.getValue();
                }
            } else if (response.getStatusLine().getStatusCode() == 304) {
                //没有修改，不做操作
                //noop
            } else {
//                logger.info("remote_ext_dict {} return bad code {}", location, response.getStatusLine().getStatusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
//            logger.error("remote_ext_dict {} error!", e, location);
        }
    }
}