package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.Represent;
import com.ming.shopping.beauty.service.entity.User;
import me.jiangcai.jpa.entity.support.Address;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author lxf
 */
public interface StoreService {
    /**
     * @return 所有的门店集合
     */
    @Transactional(readOnly = true)
    List getAllStores();

    /**
     *
     * @param storeName 门店名称
     * @param address  门店地址
     * @param telephone 门店电话
     */
    @Transactional
    void addStore(String storeName, Address address, String telephone);

    /**
     * 冻结或启用门店
     * @param id
     * @param enable 是否启用
     */
    @Transactional
    void freezeOrEnable(long id,boolean enable);

    /**
     * @param id
     * @param represent 添加门店代表,门店代表应该是
     */
    @Transactional
    void addRepresent(long id,Represent represent);

    /**
     * @param representId 冻结或启用的门店代表
     * @param enable 是否启用
     */
    @Transactional
    void freezeOrEnableRepresent(long representId,boolean enable);
}
