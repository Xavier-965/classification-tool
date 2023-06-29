package com.clps.exception;

/**
 * 缺少配置的异常
 *
 * @author tony
 */
public class NoConfigFile extends Exception {
    public NoConfigFile() {
        super("至少指定一个包内配置文件路径或者文件系统路。包内文件路径必需以/开头。");
    }
}
