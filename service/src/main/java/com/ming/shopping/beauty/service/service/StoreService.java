package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.jiangcai.jpa.entity.support.Address;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author lxf
 */
public interface StoreService {

    /**
     * 添加门店
     *
     * @param loginId    一个可登录的角色
     * @param merchantId 所属门店
     * @param name       门店名称
     * @param contact    门店联系人
     * @param address    门店地址
     * @param telephone  门店电话
     */
    @Transactional(rollbackFor = RuntimeException.class)
    void addStore(long loginId, long merchantId, String name, String telephone, String contact, Address address);

    /**
     * 添加门店管理员
     *
     * @param loginId 角色
     * @param storeId 所属门店
     */
    @Transactional(rollbackFor = RuntimeException.class)
    void addStore(long loginId, long storeId);

    /**
     * 冻结或启用门店或门店管理员
     *
     * @param id     storeId
     * @param enable 是否启用
     */
    @Transactional(rollbackFor = RuntimeException.class)
    void freezeOrEnable(long id, boolean enable);

    /**
     * 删除管理员与门店的关联
     *
     * @param managerId 门店管理员编号
     */
    @Transactional(rollbackFor = RuntimeException.class)
    void removeStoreManage(long managerId);

    /**
     * 查找门店或者门店管理员，同时检查角色是否可用，如果不可用则抛出异常
     *
     * @param id storeId
     * @return
     * @throws ApiResultException 校验错误
     */
    Store findOne(long id) throws ApiResultException;

    /**
     * 查找门店，同时检查门店是否可用，如果不可用则抛出异常
     *
     * @param storeId 门店编号
     * @return
     */
    Store findStore(long storeId);

    /**
     * 根据登录角色查找其门店
     * @param login 登录角色
     * @return
     */
    Store findByLogin(Login login);
}
