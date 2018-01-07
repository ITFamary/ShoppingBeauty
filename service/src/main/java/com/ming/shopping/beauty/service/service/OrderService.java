package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.login.Represent;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.order.OrderItem;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author lxf
 */
public interface OrderService {

    /**
     * @return 所有订单
     */
    List<MainOrder> findAll();

    /**
     * @param id 订单id
     * @return 某个订单
     */
    MainOrder findById(long id);

    /**
     * 生成订单
     * @param store 产生订单的门店
     * @param user 下单用户
     * @param represent 门店代表
     * @param itemList 该订单中的项目
     * @return
     */
    @Transactional
    MainOrder newOrder(Store store, User user, Represent represent, List<OrderItem> itemList);

    /**
     * 生成订单
     * @param store 产生订单的门店
     * @param user 下单用户
     * @param represent 门店代表
     * @param orderItem 该订单中的项目
     * @return
     */
    @Transactional
    MainOrder newOrder(Store store, User user, Represent represent, OrderItem orderItem);

    /**
     * 支付订单
     * @param id 被支付的订单.
     * @return 是否成功
     */
    @Transactional
    boolean payOrder(long id);
}
