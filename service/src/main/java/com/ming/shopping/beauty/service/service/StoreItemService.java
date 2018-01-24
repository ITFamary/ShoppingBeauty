package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.model.request.ItemSearcherBody;

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
    StoreItem addStoreItem(long storeId, long itemId, BigDecimal salesPrice, boolean recommended);


    /**
     * 根据编号查找门店项目
     * @param storeItemId
     * @return
     */
    StoreItem findStoreItem(long storeItemId);
}
