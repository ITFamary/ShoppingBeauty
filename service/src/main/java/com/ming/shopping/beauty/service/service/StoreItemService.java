package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.model.request.ItemSearcherBody;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author lxf
 */

public interface StoreItemService {


    /**
     * 查找门店所有项目
     *
     * @param searcher
     * @return
     */
    @Transactional(readOnly = true)
    List<StoreItem> findAllStoreItem(ItemSearcherBody searcher);

    /**
     * 给门店添加项目
     *
     * @param storeId     门店编号
     * @param itemId      项目编号
     * @param salesPrice  销售价
     * @param recommended 是否推荐
     * @return
     */
    @Transactional
    StoreItem addStoreItem(long storeId, long itemId, BigDecimal salesPrice, boolean recommended);

    /**
     * 给门店添加项目
     *
     * @param storeId     门店编号
     * @param itemId      项目编号
     * @param storeItem   门店项目
     * @return
     */
    @Transactional
    StoreItem addStoreItem(long storeId, long itemId, StoreItem storeItem);

    /**
     * 根据编号查找门店项目
     * @param storeItemId
     * @return
     */

    @Transactional(readOnly = true)
    StoreItem findStoreItem(long storeItemId);

    /**
     * 门店项目上架/下架
     * @param itemId 项目id 可以为null
     * @param enabled 上架/下架
     * @param storeItemId 操作的门店项目
     * @return 被操作的项目
     */

    @Transactional
    void freezeOrEnable(Long itemId, boolean enabled, long storeItemId);

    /**
     *  门店项目推荐/取消推荐
     * @param itemId 项目id 可以为null
     * @param enabled 上架/下架
     * @param storeItemId 操作的门店项目
     */
    @Transactional
    void recommended(Long itemId, boolean enabled,long storeItemId);
}
