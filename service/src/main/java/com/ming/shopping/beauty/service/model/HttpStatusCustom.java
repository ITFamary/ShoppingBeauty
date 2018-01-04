package com.ming.shopping.beauty.service.model;

import org.apache.http.HttpStatus;

/**
 * 用来定义 HttpStatus 拓展的响应码,整理一下主要有以下几种情况:
 * <p>请求不合规：返回{@link HttpStatusCustom#SC_EXPECTATION_FAILED}</p>
 * <p>不允许操作：返回{@link HttpStatusCustom#SC_FORBIDDEN}</p>
 * <p>系统正在维护：返回{@link HttpStatusCustom#SC_SYSTEM_MAINTENANCE}</p>
 * <p>系统故障：返回{@link HttpStatusCustom#SC_INTERNAL_SERVER_ERROR}</p>
 * <p>请求成功：返回{@link HttpStatusCustom#SC_OK}</p>
 * <p></p>
 * Created by helloztt on 2018/1/4.
 */
public interface HttpStatusCustom extends HttpStatus {

    /**
     * {@code 508 系统维护中}
     */
    public static final int SC_SYSTEM_MAINTENANCE = 508;
}
