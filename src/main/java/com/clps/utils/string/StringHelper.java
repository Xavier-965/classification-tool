package com.clps.utils.string;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author tony
 * 字符串操作函数集
 */
public class StringHelper {

    /**
     * 字符串去乱码，转成标准中文格式
     *
     * @param src 待转换的字符串
     * @return 转换后的字符串
     */
    public static String getCHNString(String src) {
        if (src == null) {
            return null;
        } else {
            try {
                return new String(src.getBytes("iso8859-1"), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * 字符串转半角字符串
     *
     * @param input 待转内容
     * @return
     */
    public static String toDBC(String input) {
        if (input == null) {
            return null;
        }
        char c[] = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == '\u3000') {
                c[i] = ' ';
            } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
                c[i] = (char) (c[i] - 65248);
            }
        }
        String returnString = new String(c);
        return returnString;
    }

    /**
     * 通用格式化日期方法
     * @param format 格式化字符串
     * @param calendars Calendar实例。默认使用今日
     * @return 格式化的日期字符串
     */
    public static String getFormattedDateString(String format, Calendar... calendars){
        SimpleDateFormat sdf=new SimpleDateFormat(format);
        return calendars==null || calendars.length==0? sdf.format(new Date()):sdf.format(calendars[0].getTime());
    }
}
