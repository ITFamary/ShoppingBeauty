package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.login.Represent;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.order.MainOrder_;
import com.ming.shopping.beauty.service.entity.order.OrderItem;
import com.ming.shopping.beauty.service.entity.support.OrderStatus;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.repository.MainOrderRepository;
import com.ming.shopping.beauty.service.service.MainOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lxf
 */
@Service
public class MainMainOrderServiceImpl implements MainOrderService {

    @Autowired
    private MainOrderRepository mainOrderRepository;

    @Override
    public List<MainOrder> findAll() {
        return mainOrderRepository.findAll();
    }

    @Override
    public MainOrder findById(long id) {
        MainOrder one = mainOrderRepository.findOne(id);
        if(one == null){
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.MAINORDER_NOT_EXIST));
        }
        return one;
    }

    @Override
    @Transactional
    public MainOrder newOrder(Store store, User user, Represent represent, List<OrderItem> itemList) {
        MainOrder mainOrder = new MainOrder();
        //order.setCreateTime(LocalDateTime.now());
        mainOrder.setStore(store);
        mainOrder.setUser(user);
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
    public MainOrder newOrder(Store store, User user, Represent represent, OrderItem orderItem) {
        List<OrderItem> list = new ArrayList<>();
        list.add(orderItem);
        return newOrder(store,user,represent,list);
    }

    @Override
    public boolean payOrder(long id) {
        //TODO 还不知道怎么写
        MainOrder mainOrder = findById(id);
        mainOrder.setPayTime(LocalDateTime.now());
        mainOrder.setOrderStatus(OrderStatus.success);
        return false;
    }

    @Override
    public List<MainOrder> search(Object entity, int page, int page_size) {
        Pageable pageable = new PageRequest(page - 1, page_size,new Sort(Sort.Direction.DESC, MainOrder_.createTime.getName()));
        final Page<MainOrder> result ;
        if(entity instanceof Store){
            result =mainOrderRepository.findAll((root, query, cb) -> cb.and(cb.equal(root.get(MainOrder_.store),entity)),pageable);
        }else if(entity instanceof User){
            result =mainOrderRepository.findAll((root, query, cb) -> cb.and(cb.equal(root.get(MainOrder_.user),entity)),pageable);
        }else{
            return null;
        }
        return result.getContent();
    }
}
