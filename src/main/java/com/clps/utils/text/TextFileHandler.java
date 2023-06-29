package com.clps.utils.text;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author tony
 * @date 2020-7-6
 * <h4>Demo</h4>
 * <pre>
 *         String[] modes = {
 *                 "^[0-9]{3,6},",
 *                 "\\\"[0-9]{4,6}\\\"",
 *                 "\\\"(CL-CV[0-9]{4,6})",
 *                 "\\\"[0-9]{3,6}\\\""
 *         };
 *         String[] filePaths = {
 *                 "/home/tony/projects/综合数据分析平台IDAP/codes/idap/docs/0703/ORDER_FORM.csv",
 *                 "/home/tony/projects/综合数据分析平台IDAP/codes/idap/docs/0703/RESUME_PUSH.csv",
 *                 "/home/tony/projects/综合数据分析平台IDAP/codes/idap/docs/0703/SCAN_LOGS.csv",
 *                 "/home/tony/projects/综合数据分析平台IDAP/codes/idap/docs/0703/INTERVIEW_RESULT.csv"};
 *
 *         String[] newFiles = {
 *                 "order_form_v1.csv",
 *                 "resume_push_v1.csv",
 *                 "scan_logs_v1.csv",
 *                 "interview_result_v1.csv"
 *         };
 *
 *         formatCSV4Excel(modes,filePaths,newFiles);
 * </pre>
 */
public class TextFileHandler {
/*    public static void main(String[] args) {
        String[] modes = {
                "^[0-9]{3,6},",
                "\\\"[0-9]{4,6}\\\"",
                "\\\"(CL-CV[0-9]{4,6})",
                "\\\"[0-9]{3,6}\\\""
        };
        String[] filePaths = {
                "/home/tony/projects/综合数据分析平台IDAP/codes/idap/docs/0703/ORDER_FORM.csv",
                "/home/tony/projects/综合数据分析平台IDAP/codes/idap/docs/0703/RESUME_PUSH.csv",
                "/home/tony/projects/综合数据分析平台IDAP/codes/idap/docs/0703/SCAN_LOGS.csv",
                "/home/tony/projects/综合数据分析平台IDAP/codes/idap/docs/0703/INTERVIEW_RESULT.csv"};

        String[] newFiles = {
                "order_form_v1.csv",
                "resume_push_v1.csv",
                "scan_logs_v1.csv",
                "interview_result_v1.csv"
        };

        formatCSV4Excel(modes,filePaths,newFiles);
    }*/

    /**
     * 格式化oracle导出的csv格式数据，并转换成标准的csv格式
     *
     * @param modes     新行开头模式
     * @param filePaths 待处理的原始文件路径
     * @param newFiles  生产的新的文件路径
     */
    public static void formatCSV4Excel(String[] modes, String[] filePaths, String[] newFiles) {

        for (int i = 0; i < modes.length; i++) {
            try {
                RandomAccessFile raf = new RandomAccessFile(filePaths[i], "r");
                File f = new File(newFiles[i]);
                if (f.exists()) {
                    f.delete();
                }
                RandomAccessFile newF = new RandomAccessFile(newFiles[i], "rw");
                String fulLine = "";
                String subLine = "";
                int counter = 0;
                int total = 0;
                while ((subLine = raf.readLine()) != null) {
                    subLine = new String(subLine.getBytes("iso-8859-1"), "utf-8").replace("\r", "").replace("\n", "").replace("\t", "");
                    if (counter == 0) { // 第一行不处理
                        newF.write((subLine.replace(",", "\t") + "\n").getBytes());
                    } else {
                        // start with 数字（3～6位）
                        if (isNewLine(subLine, modes[i])) {
                            if (fulLine.equals("")) {

                            } else {
                                newF.write((getCSVNewLine(fulLine) + "\n").getBytes());
                                total += 1;
                            }
                            fulLine = subLine;
                        } else {
                            fulLine += subLine;
                        }
                    }
                    counter++;
                }
                newF.write((getCSVNewLine(fulLine) + "\n").getBytes());
                System.out.println(total);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(filePaths[i] + " 处理完毕！");
        }

/*String src="675,zdd,3e好好了,786,\"ddd,fw\",98,\"\",\"900\"";
        System.out.println(getNewLine(src));*/
    }

    /**
     * 使用正则表达式，来判断是否是新行
     *
     * @param str  从原始数据文本里读出的行
     * @param mode 匹配模式（正则表达式），用于新行的识别
     * @return
     */
    private static boolean isNewLine(String str, String mode) {
        Pattern pattern = Pattern.compile(mode);
        Matcher isNewline = pattern.matcher(str);
        return isNewline.find();
    }



    /**
     * 读取csv文件到列表
     *
     * @param filePath 文件路径
     * @return List<List < String>> csv文件的数据
     * @throws IOException
     */
    public static List<List<String>> readCSVData(String filePath) throws IOException {
        List<List<String>> rslt = new ArrayList<>();
        try {
            List<String> lines = FileEncodeHelper.readFileToList(filePath);
            if (lines != null && !lines.isEmpty()) {
                for (String line : lines) {
                    String[] cells = line.split("\"(([^\",\\n 　]*[,\\n 　])*([^\",\\n 　]*\"{2})*)*[^\",\\n 　]*\"[ 　]*,[ 　]*|[^\",\\n]*[ 　]*,[ 　]*|\"(([^\",\\n 　]*[,\\n 　])*([^\",\\n 　]*\"{2})*)*[^\",\\n 　]*\"[ 　]*|[^\",\\n]*[ 　]*");
                    List<String> sublist = Arrays.asList(cells);
                    rslt.add(sublist);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rslt;
    }

    /**
     * 逐行读文件到list
     *
     * @param filePath 文件路径
     * @return List<String> 文件数据
     */
    public static List<String> loadTxtFileByLine(String filePath) {
        List<String> data = new ArrayList<>();
        List<String> single = readFileFromFileSystem(filePath);
        if (single == null) {
            single = readFileByHttp(filePath);
            if (single == null) {
                single = readFileFromPackage(filePath);
            }
        }
        if (single != null) {
            data.addAll(single);
        }
        return data.isEmpty() ? null : data;
    }

    // <editor-fold defaultstate="collapsed" desc="辅助方法">
    /**
     * 从jar包内读取文件
     * @param filePath 文件在包内的路径
     * @return List<String>
     */
    private static List<String> readFileFromPackage(String filePath) { // filePath must start with /
        List<String> data = new ArrayList<>();
        InputStream is = null;
        BufferedInputStream bis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }

        is = TextFileHandler.class.getResourceAsStream(filePath);
        bis = new BufferedInputStream(is);

        try {
            isr = new InputStreamReader(bis, "UTF-8");
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                line = new String(line.getBytes("iso8859-1"), "utf-8");
                data.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (isr != null)
                    isr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (bis != null)
                    bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data.isEmpty() ? null : data;
    }

    /**
     * 从文件系统读取文件
     * @param filePath 文件路径
     * @return
     */
    private static List<String> readFileFromFileSystem(String filePath) {
        List<String> data = null;
        File f = new File(filePath);
        if (!f.exists()) {
            f = new File("/" + filePath);
        }
        if (f.exists()) {
            data = new ArrayList<>();
            RandomAccessFile raf = null;
            try {
                String line;
                raf = new RandomAccessFile(f, "r");
                while ((line = raf.readLine()) != null) {
                    line = new String(line.getBytes("iso8859-1"), "utf8");
                    data.add(line);
                }

            } catch (IOException e) {
                return null;
            }

            return data.isEmpty() ? null : data;
        } else {
            return null;
        }
    }

    /**
     * 处理行数据并格式化成新的csv行数据
     *
     * @param src 原字符串
     * @return 转换后的字符串
     */
    private static String getCSVNewLine(String src) {
        if (src == null || "".equals(src)) {
            return src;
        }
        Pattern pattern = Pattern.compile("\\\"(.*?)\\\""); //
        Matcher matcher = pattern.matcher(src);
        while (matcher.find()) {
            String old = matcher.group();
            String n = old.replace(",", "，").replace(",", "").replace("\"", "");
            src = src.replace(old, n);
        }
        src = src.replace(",", "\t");
        return src;
    }

    /**
     * http方式读取远程文件
     * @param fileUrl 文件的uRL
     * @return
     */
    private static List<String> readFileByHttp(String fileUrl) {
        BufferedInputStream bis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        List<String> data = new ArrayList<>();
        try {
            URL url = new URL(fileUrl);
            URLConnection con = url.openConnection();
            con.setConnectTimeout(1000 * 5);
            con.setRequestProperty("Charset", "UTF-8");
            con.connect();

            bis = new BufferedInputStream(con.getInputStream());

            try {
                isr = new InputStreamReader(bis, "UTF-8");
                br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    data.add(line);
                }
            } catch (IOException e) {

            }
        } catch (IOException | IllegalArgumentException e) {

        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (isr != null)
                    isr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (bis != null)
                    bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return data.isEmpty() ? null : data;
    }
// </editor-fold>
}
