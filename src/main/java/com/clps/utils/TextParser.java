package com.clps.utils;

/**
 * @author Xavier
 * @date 2023/6/28
 */
import com.clps.utils.string.StringHelper;

import java.util.*;

/**
 * 文本处理类<br/>
 * 支持<br/>
 * <ul>
 *     <li>1. 最大正向匹配方法计算关键词词频和列表</li>
 *     <li>2. ...（持续增加）</li>
 * </ul>
 * @author Xavier
 * @date 2023/6/28
 */
public class TextParser {
    private final static List<String> letters = new ArrayList<>();
    private static int max_range_default = 25;
    private static int min_range_default = 8;

    static {
        for (int i = 0; i < 26; i++) {
            letters.add(String.valueOf((char) (97 + i)));
        }
    }

    /**
     * 使用最大正向匹配方法获取文本中出现的关键词集合
     * @param dict 参考的关键字字典
     * @param content 待分析的内容
     * @param thresholds 阀值。如果不提供，使用默认阀值；如果提供，必需提供两个整数值。程序自动分辨上下限
     * @return 提取的关键字集合
     */
    public static Set<String> getKeyWordsByMaxForwardMatch(Set<String> dict, String content, int... thresholds) {
        Map<String, Integer> rslt = getKeyWordsWithStatisticByMaxForwardMatch(dict, content, thresholds);
        if (rslt == null) {
            return null;
        } else {
            return rslt.keySet();
        }

    }

    /**
     * 使用最大正向匹配方法获取文本中出现的关键词及频次
     * @param dict 参考的关键字字典
     * @param content 待分析的内容
     * @param thresholds 阀值。如果不提供，使用默认阀值；如果提供，必需提供两个整数值。程序自动分辨上下限
     * @return 返回Key为关键词，value为频次的Map
     */
    public static Map<String, Integer> getKeyWordsWithStatisticByMaxForwardMatch(Set<String> dict, String content, int... thresholds) {
        if (content == null || "".equals(content)) {
            return null;
        }
        int max_range, min_range;
        if (thresholds == null || thresholds.length < 2) {
            max_range = max_range_default;
            min_range = min_range_default;
        } else {
            max_range = Math.max(thresholds[0], thresholds[1]);
            min_range = Math.min(thresholds[0], thresholds[1]);
        }

        content = StringHelper.toDBC(content.trim().toLowerCase());
        int offset = 0;
        int len = content.length();

        Map<String, Integer> words = new HashMap<>();
        String pre = ""; // 左侧字符
        String next = ""; // 右侧字符
        for (int indicator = 0; indicator < len; indicator++) {
            String word = null;
            int tx = 0;
            if (indicator > 0 && indicator < len - 1) {
                pre = content.substring(indicator - 1, indicator);
            }
            // 确定正向匹配的宽度
            int loop;
            String start = content.substring(indicator, indicator + 1);
            // 跳过特定字符开头的匹配
            if (" ".equals(start) || "/".equals(start) ||  "-".equals(start) || "#".equals(start)) { // 跳过空格开头的匹配
                continue;
            }
            if (letters.contains(start)|| ".".equals(start)) {
                loop = max_range;
            } else {
                loop = min_range;
            }

            for (int i = 1; i <= loop; i++) {
                if (indicator + i < len) {
                    next = content.substring(indicator + offset + i, indicator + offset + i + 1);
                } else {
                    next = "";
                }

                if (indicator + offset + i > len) {
                    continue;
                }
                String tmp = content.substring(indicator + offset, indicator + offset + i);
                if (dict.contains(tmp)) {
                    boolean l = letters.contains(pre);
                    boolean r = letters.contains(next);
                    if (!l && !r) {
                        tx = i;
                        word = tmp;
                    }else{
                        if(l && !r){ // 判断tmp第一个字符是否为字母
                            if(!letters.contains(String.valueOf(tmp.charAt(0)))) {
                                tx = i;
                                word = tmp;
                            }
                        }else if(!l && r){ // 判断tmp的最后一个字符是否字母，如果不是字母，返回；如果是字母，继续增加
                            if(!letters.contains(String.valueOf(tmp.charAt(tmp.length()-1)))){
                                tx=i;
                                word=tmp;
                            }
                        }else{ // 判断tmp的第一个和最后一个字符是否为字母
                            if(!letters.contains(String.valueOf(tmp.charAt(tmp.length()-1)))
                                    && !letters.contains(String.valueOf(tmp.charAt(0)))){
                                tx=i;
                                word=tmp;
                            }
                        }
                    }
                }
            }

            offset = tx;
            if (word != null) {
                if (words.containsKey(word))
                    words.put(word, words.get(word) + 1);
                else
                    words.put(word, 1);
            }
            indicator += (offset == 0 ? 0 : offset - 1);
            offset = 0;
        }
        return words;
    }
}
