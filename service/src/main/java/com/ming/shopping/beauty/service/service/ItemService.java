package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author lxf
 */
public interface ItemService {

    /**
     * @return 查询所有的项目
     */
    List<Item> findAll();

    /**
     * @param id 项目id
     * @return 项目
     */
    Item findById(long id);

    /**
     * 添加新的项目
     * @param merchant 所属的商家
     * @param thumbnailUrl 缩略图
     * @param name 项目名称
     * @param itemType 项目类型
     * @param price 原价格
     * @param salesPrice 销售价格
     * @param costPrice 结算价
     * @param description 简单描述
     * @param richDescription 详细描述
     * @param recommended  是否推荐
     * @return
     */
    @Transactional
    Item addItem(Merchant merchant, String thumbnailUrl, String name, String itemType, BigDecimal price,
                 BigDecimal salesPrice, BigDecimal costPrice, String description, String richDescription, boolean recommended);

    /**
     * 修改项目是否冻结/解冻
     * @param id 项目id
     * @param enable 是否冻结
     * @return
     */
    @Transactional
    Item freezeOrEnable(long id,boolean enable);

    /**
     * 修改项目 是否在系统中展示
     * @param id 项目id
     * @param deleted 是否展示
     * @return
     */
    @Transactional
    Item showOrHidden(long id,boolean deleted);

    /**
     * 设置项目所属商户
     * @param id 项目id
     * @param merchant  所属商户
     * @return
     */
    @Transactional
    Item setMerchant(long id,Merchant merchant);
}
