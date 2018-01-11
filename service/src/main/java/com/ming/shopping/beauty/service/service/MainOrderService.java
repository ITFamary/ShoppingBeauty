package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.login.Represent;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.order.OrderItem;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author lxf
 */
public interface MainOrderService {


    /**
     * @param id 订单id
     * @return 某个订单
     */
    MainOrder findById(long id);

    /**
     * 给用户增加一个空的订单
     * @param user
     * @return
     */
    @Transactional
    MainOrder newEmptyOrder(User user);

    /**
     *
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

    /**
     * 根据登录用户查询订单列表
     * @param entity 身份
     * @param page 页码
     * @param page_size 每页多少
     * @return MainOrder集合
     */
    List<MainOrder> search(Object entity , int page , int page_size);
}
