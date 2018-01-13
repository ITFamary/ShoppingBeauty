package com.ming.shopping.beauty.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author helloztt
 */
@AllArgsConstructor
@Getter
public enum ResultCodeEnum {
    /**
     * 调用第三方接口返回的错误，就不定义每种错误了，主要返回错误信息
     */
    THIRD_ERROR(333,""),
    /**
     * 数据格式错误，具体哪个格式错误需要根据场景返回
     */
    REQUEST_DATA_ERROR(999,"{0}格式错误"),
    /**
     * 注册、登录、权限相关：[1000,1999]
     */
    MOBILE_EXIST(1001,"手机号已经注册"),
    MESSAGE_NOT_FULL(1002,"注册信息不完成"),
    LOGIN_NOT_EXIST(1003,"账号不存在"),
    MERCHANT_NOT_EXIST(1004,"商户不存在"),
    STORE_NOT_EXIST(1005,"门店不存在"),
    USERNAME_ERROR(1006,"手机号错误"),
    LOGIN_NOT_ENABLE(1007,"账号不可用"),
    MERCHANT_NOT_ENABLE(1008,"商户不可用"),
    STORE_NOT_ENABLE(1009,"门店不可用"),
    MANAGE_NOT_ENABLE(1010,"操作员不可用"),
    ALREADY_MANAGEABLE(1011,"请勿重复操作"),
    REPRESENT_NOT_EXIST(1012,"门店代表不存在"),
    /**
     * 用户、推荐：[2000,2999]
     */
    LOGIN_MERCHANT_EXIST(2000,"该账号已是商户操作员"),
    MERCHANT_CANNOT_DELETE(2001,"商户不可删除"),
    LOGIN_STORE_EXIST(2002,"该账号已是门店操作员"),
    STORE_CANNOT_DELETE(2003,"门店不可删除"),
    LOGIN_REPRESENT_EXIST(2004,"该账号已是门店代表"),
    USER_NOT_ACTIVE(2005,"充值后才能激活会员卡"),
    /**
     * 项目、门店：[3000,3999]
     */
    Item_Not_EXIST(3001,"项目不存在或已删除"),
    STORE_ITEM_PRICE_ERROR(3002,"销售价必须大于项目销售价"),
    /**
     * 订单相关：[4000,4999]
     */
    MAINORDER_NOT_EXIST(4001,"订单不存在"),
    /**
     * 支付、充值：[5000,5999]
     */
    CARD_NOT_EXIST(5000,"充值卡无效"),
    CARD_ALREADY_USED(5001,"充值卡失效"),

    
    ;
    private int code;
    private String message;
}
