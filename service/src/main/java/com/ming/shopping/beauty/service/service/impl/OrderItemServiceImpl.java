package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.order.Order;
import com.ming.shopping.beauty.service.entity.order.OrderItem;
import com.ming.shopping.beauty.service.repository.OrderItemRepository;
import com.ming.shopping.beauty.service.repository.OrderRepository;
import com.ming.shopping.beauty.service.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lxf
 */
@Service
public class OrderItemServiceImpl implements OrderItemService {
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> findByOrderId(long id) {
        Order order = orderRepository.findOne(id);
        List<OrderItem> byOrder = orderItemRepository.findByOrder(order);
        return byOrder;
    }

    @Override
    @Transactional
    public OrderItem newOrderItem(Item item, int num) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setName(item.getName());
        orderItem.setPrice(item.getPrice());
        orderItem.setSalesPrice(item.getSalesPrice());
        orderItem.setCostPrice(item.getCostPrice());
        orderItem.setNum(num);
        return orderItemRepository.save(orderItem);
    }

    @Override
    @Transactional
    public List<OrderItem> newOrderItems(Map<Item, Integer> itemMap) {
        Set<Item> items = itemMap.keySet();
        List<OrderItem> listOrderItem = new ArrayList<>();
        for (Item item : items) {
            Integer num = itemMap.get(item);
            listOrderItem.add(newOrderItem(item,num));
        }
        return listOrderItem;
    }

}
