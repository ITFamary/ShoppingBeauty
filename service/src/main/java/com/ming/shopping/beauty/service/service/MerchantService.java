package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.jiangcai.jpa.entity.support.Address;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author
 */
public interface MerchantService {

    /**
     * 把某个角色设置为商户的超级管理员
     *
     * @param loginId   一个可登录的角色
     * @param name      商户名称
     * @param telephone 商户电话
     * @param contact   商户联系人
     * @param address   商户地址
     * @return
     * @throws ApiResultException
     */
    @Transactional(rollbackFor = RuntimeException.class)
    Merchant addMerchant(long loginId, String name, String telephone, String contact, Address address) throws ApiResultException;

    /**
     * 把某个角色设置为商户
     * @param loginId 一个可登录的角色
     * @param merchant 商户基本信息，包含名称、电话、联系人、地址
     * @return
     * @throws ApiResultException
     */
    @Transactional(rollbackFor = RuntimeException.class)
    Merchant addMerchant(long loginId,Merchant merchant) throws ApiResultException;

    /**
     * 添加商户的管理员
     *
     * @param loginId
     * @param merchantId
     * @return
     * @throws ApiResultException
     */
    @Transactional(rollbackFor = RuntimeException.class)
    Merchant addMerchant(long loginId, long merchantId) throws ApiResultException;

    /**
     * 冻结或启用商户
     *
     * @param id
     * @param enable 是否启用
     * @throws ApiResultException
     */
    @Transactional(rollbackFor = RuntimeException.class)
    void freezeOrEnable(long id, boolean enable) throws ApiResultException;

    /**
     * 删除角色与商户的关联
     *
     * @param managerId 商户管理员
     * @throws ApiResultException
     */
    @Transactional(rollbackFor = RuntimeException.class)
    void removeMerchantManage(long managerId) throws ApiResultException;

    /**
     * 查找商户或商户管理员，同时检查商户或商户管理员是否可用，如果不可用就抛出异常
     *
     * @param merchantId 商户或商户管理员
     * @return
     * @throws ApiResultException
     */
    Merchant findOne(long merchantId) throws ApiResultException;

    /**
     * 查找商户，同时检查商户是否可用，如果不可用就抛出异常
     *
     * @param merchantId
     * @return
     * @throws ApiResultException
     */
    Merchant findMerchant(long merchantId) throws ApiResultException;

}
