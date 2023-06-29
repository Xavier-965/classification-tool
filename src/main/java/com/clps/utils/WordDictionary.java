package com.clps.utils;

import com.clps.exception.NoConfigFile;
import com.clps.utils.cities.CityHelper;
import com.clps.utils.print.Color;
import com.clps.utils.print.Printer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WordDictionary {

    /*
     * 词典单子实例
     */
    private static Logger logger = LogManager.getLogger(WordDictionary.class);
    private static ScheduledExecutorService skillPool = Executors.newScheduledThreadPool(1);
    private static ScheduledExecutorService businessPool = Executors.newScheduledThreadPool(1);
    private static ScheduledExecutorService synonymPool = Executors.newScheduledThreadPool(1);
    private static ScheduledExecutorService citiesPool = Executors.newScheduledThreadPool(1);
    private static ScheduledExecutorService skillDescPool = Executors.newScheduledThreadPool(1);

    private static Map<String, Double> idfDictMap = new ConcurrentHashMap<>();
    private static Map<String, String> skillDescMap = new ConcurrentHashMap<>();
    private static Set<String> businessDictSet = Collections.synchronizedSet(new HashSet());
    private static Set<String> skillDictSet = Collections.synchronizedSet(new HashSet());
    private static Map<String, String> synonymDict = new ConcurrentHashMap<>();
    private static Set<String> cityDict = Collections.synchronizedSet(new HashSet());

    private static String idfDictPath = "";
    private static boolean idfDictHot = false;
    private static String businessDictPath = "";
    private static boolean bizDictHot = false;
    private static String skillDictPath = "";
    private static boolean skillDictHot = false;
    private static String synonymDictPath = "";
    private static boolean synDictHot = false;
    private static String cityDictPath = "";
    private static boolean citiesDictHot = false;

    private static String globalCitiesDictPath = "";
    private static boolean globalCitiesDictHot = false;

    private static boolean skillDescHot = false;
    private static String skillDescPath = "";
    private static WordDictionary singleton;
    private static Properties properties;

    static { // 加载词库数据（）
        try {
            properties = ConfigurationLoader.getDefaultProperties();

            idfDictPath = properties.getProperty("resume.http.idf_dict.path").trim();
            businessDictPath = properties.getProperty("resume.http.business_dict.path").trim();
            synonymDictPath = properties.getProperty("resume.http.synonym_dict.path").trim();
            cityDictPath = properties.getProperty("resume.http.city_dict.path").trim();
            globalCitiesDictPath = properties.getProperty("resume.http.globalCities").trim();

            skillDictPath = properties.getProperty("resume.http.skill_dict.path").trim();
            skillDescPath = properties.getProperty("resume.http.skill_desc.path").toString().trim();
        }catch (NoConfigFile e){
            Printer.print(e.getMessage(), Color.Red);
        }
    }

    /**
     * 词典初始化 由于IK Analyzer的词典采用Dictionary类的静态方法进行词典初始化
     * 只有当Dictionary类被实际调用时，才会开始载入词典， 这将延长首次分词操作的时间 该方法提供了一个在应用加载阶段就初始化字典的手段
     *
     * @return Dictionary
     */
    public static synchronized void initial() {
        if (singleton == null) {
            synchronized (WordDictionary.class) {
                if (singleton == null) {
                    singleton = new WordDictionary();

                    singleton.loadSkillDescDict();
                    singleton.loadIDFDict();
                    singleton.loadSkillDict();
                    singleton.loadBusinessDict();
                    singleton.loadSynonymDict();
                    singleton.loadCityDict();
                    singleton.loadGlobalCitiesDict();
                }
            }
        }
    }

    /**
     * 加载主词典及扩展词典
     */
    private void loadSkillDict() {
        if (businessDictPath == null || "".equals(businessDictPath)) {
            Printer.println("请注意：技能词典未加载", Color.Red);
            return;
        }
        try {
            skillDictSet.addAll(ResourceFileLoader.loadFileToSet(skillDictPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void loadBusinessDict() {
        if (businessDictPath == null || "".equals(businessDictPath)) {
            Printer.println("请注意：城市词典未加载", Color.Red);
            return;
        }
        try {
            businessDictSet.addAll(ResourceFileLoader.loadFileToSet(businessDictPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadIDFDict() {
        if (idfDictPath == null || "".equals(idfDictMap)) {
            Printer.println("请注意：TF-IDF词典未加载", Color.Red);
            return;
        }
        try {
            idfDictMap.putAll(ResourceFileLoader.loadFileToMap(idfDictPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadSkillDescDict() {
        if (skillDescPath == null ) {
            Printer.println("请注意：技能描述词典路径未加载", Color.Red);
            return;
        }
        ResourceFileLoader.loadFileToMap(skillDescMap,skillDescPath,",");
    }
    private void loadSynonymDict() {
        if (synonymDictPath == null || "".equals(synonymDictPath)) {
            Printer.println("请注意：同义词词典为加载", Color.Red);
            return;
        }
        ResourceFileLoader.loadFileToMapSynDict(synonymDict, synonymDictPath, "=", true);
    }
    private void loadCityDict() {
        if (cityDictPath == null || "".equals(cityDictPath)) {
            Printer.println("请注意：城市词典未加载");
            return;
        }
        try {
            cityDict.addAll(ResourceFileLoader.loadFileToSet(cityDictPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void loadGlobalCitiesDict() {
        if (globalCitiesDictPath == null || "".equals(globalCitiesDictPath)) {
            Printer.println("请注意：全球城市词典路径未赋值");
            return;
        }
        try {
            CityHelper.init(globalCitiesDictPath);
        } catch (Exception e) {
            Printer.println("请注意：全球城市词典初始化失败");
            throw new RuntimeException(e);
        }
    }


    /**
     * 获取词典单子实例
     *
     * @return Dictionary 单例对象
     */
    public static WordDictionary getSingleton() {
        if (singleton == null) {
            initial();
        }
        return singleton;
    }
    //限制Dictionary实例的获取方式
    private WordDictionary() {
    }
    private void setHotUpdate() {
        if (skillDictHot) skillPool.scheduleAtFixedRate(new Monitor(skillDictPath), 0, 60, TimeUnit.SECONDS);
        if (bizDictHot) businessPool.scheduleAtFixedRate(new Monitor(businessDictPath), 0, 60, TimeUnit.SECONDS);
        if (synDictHot) synonymPool.scheduleAtFixedRate(new Monitor(synonymDictPath), 0, 60, TimeUnit.SECONDS);
        if (citiesDictHot) citiesPool.scheduleAtFixedRate(new Monitor(cityDictPath), 0, 60, TimeUnit.SECONDS);
        if (skillDescHot) skillDescPool.scheduleAtFixedRate(new Monitor(skillDescPath), 0, 60, TimeUnit.SECONDS);
    }
    /**
     * 重新加载所有词典
     *
     * @param
     * @return
     */
    public void reLoadDict(String location) {
//        logger.info("重新加载词典...");
        // 新开一个实例加载词典，减少加载过程对当前词典使用的影响
        if (location.equals(idfDictPath)) {
//            logger.info("正在重新加载词典idfDict...");
//            WordDictionary tmpDict = new WordDictionary();
            idfDictMap.clear();
            this.loadIDFDict();
        }

        if (location.equals(skillDictPath)) {
//            logger.info("正在重新加载词典skillDict...");
//            WordDictionary tmpDict = new WordDictionary();
            skillDictSet.clear();
            this.loadSkillDict();
        }

        if (location.equals(businessDictPath)) {
//            logger.info("正在重新加载词典businessDict...");
//            WordDictionary tmpDict = new WordDictionary();
            businessDictSet.clear();
            this.loadBusinessDict();
        }

        if (location.equals(synonymDictPath)) {
//            logger.info("正在重新加载同义词词典SynonymDict...");
            this.loadSynonymDict();
        }

        if (location.equals(cityDictPath)) {
//            logger.info("正在重新加载城市词典CityDict...");
            this.loadCityDict();
        }
        if (location.equals(skillDescPath)) {
//            logger.info("正在重新加载城市词典CityDict...");
            this.loadSkillDescDict();
        }
//        logger.info("重新加载词典完毕...");
    }

    public void setAllHot() {
        if (!idfDictHot) WordDictionary.idfDictHot = true;
        if (!bizDictHot) WordDictionary.bizDictHot = true;
        if (!skillDictHot) WordDictionary.skillDictHot = true;
        if (!citiesDictHot) WordDictionary.citiesDictHot = true;
        if (!skillDescHot) WordDictionary.skillDescHot = true;
        setHotUpdate();
    }

    public void setIdfDictHot(boolean idfDictHot) {
        if (!WordDictionary.idfDictHot && idfDictHot) {
            WordDictionary.idfDictHot = idfDictHot;
            setHotUpdate();
        }
    }

    public void setBizDictHot(boolean bizDictHot) {
        if (!WordDictionary.bizDictHot && bizDictHot) {
            WordDictionary.bizDictHot = bizDictHot;
            setHotUpdate();
        }
    }

    /**
     * 开启技能词典的热更新功能
     *
     * @param
     * @return
     */
    public void setSkillDictHot(boolean skillDictHot) {
        if (!WordDictionary.skillDictHot && skillDictHot) {
            WordDictionary.skillDictHot = skillDictHot;
            setHotUpdate();
        }
    }

    public void setSynDictHot(boolean synDictHot) {
        if (!WordDictionary.synDictHot && synDictHot) {
            WordDictionary.synDictHot = synDictHot;
            setHotUpdate();
        }
    }
    public void setSkillDescDictHot(boolean skillDescDictHot) {
        if (!WordDictionary.skillDescHot && skillDescDictHot) {
            WordDictionary.skillDescHot = skillDescDictHot;
            setHotUpdate();
        }
    }
    public void setCitiesDictHot(boolean citiesDictHot) {
        if (!WordDictionary.citiesDictHot && citiesDictHot) {
            WordDictionary.citiesDictHot = citiesDictHot;
            setHotUpdate();
        }
    }

    public static Map<String, Double> getIdfDictMap() {
        return idfDictMap;
    }

    public static Map<String, String> getSkillDescMap() {
        return skillDescMap;
    }

    public static Set<String> getBusinessDictSet() {
        return businessDictSet;
    }

    public static Set<String> getSkillDictSet() {
        return skillDictSet;
    }

    public static Map<String, String> getSynonymDict() {
        return synonymDict;
    }

    public static Set<String> getCityDict() {
        return cityDict;
    }

    public static void main(String[] args) {
        initial();
        String str="hadoop java 金融恢复解放军坎大哈三剑客地方哈祭祀坑";
       /* System.out.println(WordDictionary.getBusinessDictSet().size());
        Set<String> words = TextParser.getKeyWordsByMaxForwardMatch(WordDictionary.getSelfidfDictSet(), str);
        for (String word : words) {
            System.out.println(word);
        }*/
    }
}
