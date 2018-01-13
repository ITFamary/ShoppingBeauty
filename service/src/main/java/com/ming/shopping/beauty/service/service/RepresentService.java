package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.login.Represent;
import org.springframework.transaction.annotation.Transactional;

/**
 * 门店代表相关
 *
 * @author helloztt
 */
public interface RepresentService {
    /**
     * 添加门店代表
     *
     * @param loginId 角色
     * @param storeId 门店
     * @return
     */
    @Transactional(rollbackFor = RuntimeException.class)
    Represent addRepresent(long loginId, long storeId);

    /**
     * 冻结或启用门店代表
     *
     * @param id     storeId
     * @param enable 是否启用
     */
    @Transactional(rollbackFor = RuntimeException.class)
    void freezeOrEnable(long id, boolean enable);

    /**
     * 查找门店代表
     *
     * @param id
     * @return
     */
    Represent findOne(long id);
}
