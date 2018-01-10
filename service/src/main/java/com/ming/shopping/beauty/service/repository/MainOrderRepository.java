package com.ming.shopping.beauty.service.repository;

import com.ming.shopping.beauty.service.entity.order.MainOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MainOrderRepository extends JpaRepository<MainOrder,Long>,JpaSpecificationExecutor<MainOrder> {
}
