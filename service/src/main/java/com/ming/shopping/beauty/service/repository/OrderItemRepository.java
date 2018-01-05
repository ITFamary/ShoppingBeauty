package com.ming.shopping.beauty.service.repository;

import com.ming.shopping.beauty.service.entity.order.Order;
import com.ming.shopping.beauty.service.entity.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * @author lxf
 */
public interface OrderItemRepository extends JpaRepository<OrderItem,Long>,JpaSpecificationExecutor<OrderItem> {

    List<OrderItem> findByOrder(Order order);
}
