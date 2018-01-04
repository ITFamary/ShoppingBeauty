package com.ming.shopping.beauty.service.repository;

import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Created by helloztt on 2018/1/4.
 */
public interface RechargeCardRepository extends JpaRepository<RechargeCard,Long>,JpaSpecificationExecutor<RechargeCard> {
}
