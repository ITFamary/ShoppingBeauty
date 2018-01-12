package com.ming.shopping.beauty.service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming.shopping.beauty.service.entity.login.*;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.order.OrderItem;
import com.ming.shopping.beauty.service.entity.support.OrderStatus;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.repository.MainOrderRepository;
import com.ming.shopping.beauty.service.service.MainOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author lxf
 */
@Service
public class MainOrderServiceImpl implements MainOrderService {

    @Autowired
    private MainOrderRepository mainOrderRepository;
    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public MainOrder findById(long id) {
        MainOrder one = mainOrderRepository.findOne(id);
        if (one == null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.MAINORDER_NOT_EXIST));
        }
        return one;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public MainOrder newEmptyOrder(User user) {
        //先看看这个用户有没有空的订单
        MainOrder mainOrder = mainOrderRepository.findEmptyOrderByPayer(user.getId());
        if(mainOrder != null){
            return mainOrder;
        }
        mainOrder = new MainOrder();
        mainOrder.setPayer(user);
        mainOrder.setCreateTime(LocalDateTime.now());
        //空的订单
        mainOrder.setOrderStatus(OrderStatus.EMPTY);
        return mainOrderRepository.save(mainOrder);
    }

    @Override
    @Transactional
    public MainOrder supplementOrder(long orderId, Store store, Represent represent, List<OrderItem> itemList) {
        //门店代表扫码后，把List<OrderItem>塞到了这个订单里，并修改MainOrder
        MainOrder mainOrder = mainOrderRepository.getOne(orderId);
        mainOrder.setStore(store);
        mainOrder.setRepresent(represent);
        mainOrder.setOrderItemList(itemList);
        //待付款
        mainOrder.setOrderStatus(OrderStatus.forPay);
        //未结算
        mainOrder.setSettled(false);
        return mainOrderRepository.save(mainOrder);
    }

    @Override
    @Transactional
    public MainOrder supplementOrder(long orderId, Store store, Represent represent, OrderItem orderItem) {
        List<OrderItem> list = new ArrayList<>();
        list.add(orderItem);
        return supplementOrder(orderId, store, represent, list);
    }

    @Override
    public boolean payOrder(long id) {
        //TODO 还不知道怎么写
        MainOrder mainOrder = findById(id);
        mainOrder.setPayTime(LocalDateTime.now());
        mainOrder.setOrderStatus(OrderStatus.success);
        return false;
    }


}
