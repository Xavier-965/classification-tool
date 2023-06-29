package com.clps.utils.print;

/**
 * 打印字体颜色
 * 参考 https://blog.csdn.net/ShewMi/article/details/78992458
 * @author 杨海山
 */
public class Printer {
    /**
     * 打印带颜色的信息到控制台
     * @param content 待输出的信息
     * @param colors 字体颜色
     */
    public static void println(String content, Color ... colors) {
        String code = "32";
        if (colors != null && colors.length > 0) {
            code = colors[0].getCode();
        }
        System.out.println("\u001b[1;" + code +"m"+ content + "\u001b[0m");
    }

    public static String print(String content, Color ... colors) {
        String code = "32";
        if (colors != null && colors.length > 0) {
            code = colors[0].getCode();
        }
        String msg="\u001b[1;" + code +"m"+ content + "\u001b[0m";
        System.out.print(msg);
        return msg;
    }

    /**
     * 打印带背景色的信息到控制台
     * @param content 要输出的信息
     * @param color 字体颜色
     * @param bgColor 背景色
     */
    public static void printWithBg(String content, Color color, BGColor bgColor) {
        System.out.println("\u001b[1;" + color.getCode()+";"+bgColor.getCode()+"m" + content + "\u001b[0m");
    }
}

