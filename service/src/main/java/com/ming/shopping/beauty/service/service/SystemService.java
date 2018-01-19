package com.ming.shopping.beauty.service.service;

/**
 * 系统服务；它不依赖任何玩意儿
 *
 * @author helloztt
 */
public interface SystemService {


    /**
     * 一些请求地址
     */
    String LOGIN = "/auth";
    String TO_LOGIN = "/toLogin";

    /**
     * @param uri 传入uri通常/开头
     * @return 完整路径
     */
    String toUrl(String uri);
}
