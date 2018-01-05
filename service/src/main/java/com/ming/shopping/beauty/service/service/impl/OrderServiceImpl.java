package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.login.Represent;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.entity.order.Order;
import com.ming.shopping.beauty.service.entity.order.OrderItem;
import com.ming.shopping.beauty.service.entity.support.OrderStatus;
import com.ming.shopping.beauty.service.repository.OrderRepository;
import com.ming.shopping.beauty.service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lxf
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Order findById(long id) {
        return orderRepository.findOne(id);
    }

    @Override
    @Transactional
    public Order newOrder(Store store, User user, Represent represent, List<OrderItem> itemList) {
        Order order = new Order();
        //order.setCreateTime(LocalDateTime.now());
        order.setStore(store);
        order.setUser(user);
        order.setRepresent(represent);
        order.setOrderItemList(itemList);
        //待付款
        order.setOrderStatus(OrderStatus.forPay);
        //未结算
        order.setSettled(false);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order newOrder(Store store, User user, Represent represent, OrderItem orderItem) {
        List<OrderItem> list = new ArrayList<>();
        list.add(orderItem);
        return newOrder(store,user,represent,list);
    }

    @Override
    public boolean payOrder(long id) {
        //TODO 还不知道怎么写
        Order order = findById(id);
        order.setPayTime(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.success);
        return false;
    }
}
