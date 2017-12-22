package com.ming.shopping.beauty.service.service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author
 */
public interface MerchantService {
    /**
     * @return 所有商户的List集合
     */
    @Transactional(readOnly = true)
    List getAllMerchant();

    /**
     * 添加商户
     * @param merchantName 商户名称
     * @param password 密码
     */
    @Transactional
    void addMerchant(String merchantName,String password);

    /**
     * 重置密码
     * @param id 需要重置的用户id
     * @param password 新密码
     */
    @Transactional
    void resetPassword(long id,String password);

    /**
     * 冻结或启用商户
     * @param id
     * @param enable 是否启用
     */
    @Transactional
    void freezeOrEnable(long id,boolean enable);

}
