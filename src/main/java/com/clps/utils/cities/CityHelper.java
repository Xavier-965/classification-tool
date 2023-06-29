package com.clps.utils.cities;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.clps.utils.text.FileEncodeHelper;

/**
 * 从csv文本文件读取世界城市数据<br/>
 *
 * @author Tony
 * updated: 2023-03-13
 * <p>
 * csv格式说明<br/>
 * 国家,省份,城市<br/>
 * 其中城市用|分隔多个名称或简写,但第一个必须是标准城市名称. 例如： 中国,广东,广州|GZ
 */
public class CityHelper {
    private static Map<String, String> cityInfo = new ConcurrentHashMap<>();
    private static List<Set<String>> neighborCities = new ArrayList<>();

    static {
        Set<String> changJiangDelta = new HashSet<>();
        String cities="上海、南京、无锡、徐州、常州、苏州、南通、连云港、淮安、盐城、扬州、镇江、泰州、宿迁、杭州、宁波、温州、绍兴、湖州、" +
                "嘉兴、金华、衢州、舟山、台州、丽水、合肥、芜湖、马鞍山、铜陵、池州、安庆、宣城、滁州、蚌埠、淮北、淮南、宿州、阜阳、亳州、" +
                "六安、黄山";
        changJiangDelta.addAll(Arrays.asList(cities.split("、")));

        Set<String> zhuJiangDelta = new HashSet<>();
        cities="广州、佛山、肇庆、深圳、东莞、惠州、珠海、中山、江门";
        zhuJiangDelta.addAll(Arrays.asList(cities.split("、")));

        Set<String> northEastDelta = new HashSet<>();
        cities="哈尔滨、牡丹江、齐齐哈尔、大庆、鸡西、佳木斯、鹤岗、双鸭山、伊春、七台河、黑河、 绥化、沈阳、大连、鞍山、抚顺、本溪、丹东、锦州、营口、阜新、辽阳、盘锦、铁岭、朝阳、葫芦岛、长春、吉林、四平、辽源、通化、白山、松原、白城";
        northEastDelta.addAll(Arrays.asList(cities.split("、")));

        Set<String> jingJinTangDelta = new HashSet<>();
        cities="北京、天津、唐山";
        jingJinTangDelta.addAll(Arrays.asList(cities.split("、")));

        neighborCities.add(changJiangDelta);
        neighborCities.add(zhuJiangDelta);
        neighborCities.add(northEastDelta);
        neighborCities.add(jingJinTangDelta);
    }

    public static synchronized void init(String fileName) {
        try {
            List<String> dat = FileEncodeHelper.readFileToList(fileName);
            for (String t : dat) {
                String r = t.replace("省", "").replace("市", "");
                String[] arr = r.split(",", -1);
                String country = arr[0].toLowerCase();
                String p = arr[1].toLowerCase();
                String strCity = arr[2].toLowerCase();
                String[] cs = strCity.split("\\|");

                if (!cityInfo.containsKey(country)) {
                    cityInfo.put(country, arr[0] + ",,");
                }
                if (!cityInfo.containsKey(p)) {

                    Set<String> cities = new HashSet<>();
                    Collections.addAll(cities, cs);
                    if (cities.contains(p)) {
                        cityInfo.put(p, arr[0] + "," + p + "," + p);
                    } else {
                        cityInfo.put(p, arr[0] + "," + arr[1] + ",");
                    }
                }

                // if(!cityInfo.containsKey(arr[2].toLowerCase())){
                //     cityInfo.put(arr[2].toLowerCase(),r);
                // }
                for (String c : cs) {
                    if (!cityInfo.containsKey(c)) {
                        cityInfo.put(c, country + "," + p + "," + cs[0]);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param condition
     * @param useNearCity 是否使用临近城市，默认false，不使用
     * @return
     */
    public static String queryCitiInfo(String condition, boolean... useNearCity) {
        condition = condition.replace("省", "").replace("市", "").toLowerCase();
        String result = cityInfo.get(condition);
        boolean enableNearCity=false;
        if(useNearCity!=null && useNearCity.length>0){
            enableNearCity=useNearCity[0];
        }
        if (result == null && condition!=null && condition.length()>0) {
            for (String k : cityInfo.keySet()) {
                if (k.contains(condition)) {
                    result = cityInfo.get(k);
                    break;
                }
            }
        }

        if(enableNearCity){
            if(result!=null){
                String province=result.split(",",3)[1];
                if("广东".equals(province)){
                    result="中国,广东,";
                }else if("上海".equals(province) ||"江苏".equals(province) ||"浙江".equals(province)){
                    result="中国,上海,上海;中国,江苏,;中国,浙江,";
                }else if("天津".equals(province) ||"北京".equals(province) ){
                    result="中国,天津,天津;中国,北京,北京";
                }else if("黑龙江".equals(province) ||"辽宁".equals(province) ||"吉林".equals(province)){
                    result="中国,黑龙江,;中国,辽宁,;中国,吉林,";
                }
            }
        }

        return result;
    }

    public static void main(String[] args) {
        CityHelper.init("http://172.16.3.21/resumeDictFile/globalCities.csv");
        String condition = "深圳";
        String rslt = CityHelper.queryCitiInfo(condition,true);
        System.out.println("Input: " + condition);
        System.out.println("Output: " + rslt);

        System.out.println("---------------------------");
        condition = "SZ";
        rslt = CityHelper.queryCitiInfo(condition);
        System.out.println("Input: " + condition);
        System.out.println("Output: " + rslt);

        System.out.println("---------------------------");
        condition = "上海";
        rslt = CityHelper.queryCitiInfo(condition);
        System.out.println("Input: " + condition);
        System.out.println("Output: " + rslt);

        System.out.println("---------------------------");
        condition = "SH";
        rslt = CityHelper.queryCitiInfo(condition);
        System.out.println("Input: " + condition);
        System.out.println("Output: " + rslt);

        System.out.println("---------------------------");
        condition = "HK";
        rslt = CityHelper.queryCitiInfo(condition);
        System.out.println("Input: " + condition);
        System.out.println("Output: " + rslt);

        System.out.println("---------------------------");
        condition = "香港";
        rslt = CityHelper.queryCitiInfo(condition);
        System.out.println("Input: " + condition);
        System.out.println("Output: " + rslt);

        System.out.println("---------------------------");
        condition = "广东";
        rslt = CityHelper.queryCitiInfo(condition);
        System.out.println("Input: " + condition);
        System.out.println("Output: " + rslt);

        System.out.println("---------------------------");
        condition = "中国";
        rslt = CityHelper.queryCitiInfo(condition);
        System.out.println("Input: " + condition);
        System.out.println("Output: " + rslt);

        System.out.println("---------------------------");
        condition = "中xxx";
        rslt = CityHelper.queryCitiInfo(condition);
        System.out.println("Input: " + condition);
        System.out.println("Output: " + rslt);

        System.out.println("---------------------------");
        condition = "大连";
        rslt = CityHelper.queryCitiInfo(condition,true);
        System.out.println("Input: " + condition);
        System.out.println("Output: " + rslt);
    }
}