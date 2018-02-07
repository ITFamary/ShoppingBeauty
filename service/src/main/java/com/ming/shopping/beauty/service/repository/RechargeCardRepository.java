package com.ming.shopping.beauty.service.repository;

import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.entity.login.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Created by helloztt on 2018/1/4.
 */
public interface RechargeCardRepository extends JpaRepository<RechargeCard,Long>,JpaSpecificationExecutor<RechargeCard> {
}
