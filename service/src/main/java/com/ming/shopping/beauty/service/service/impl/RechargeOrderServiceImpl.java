package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.entity.order.RechargeOrder;
import com.ming.shopping.beauty.service.repository.RechargeOrderRepository;
import com.ming.shopping.beauty.service.service.RechargeOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author helloztt
 */
@Service
public class RechargeOrderServiceImpl implements RechargeOrderService {
    @Autowired
    private RechargeOrderRepository rechargeOrderRepository;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public RechargeOrder newOrder(User payer, BigDecimal amount) {
        RechargeOrder rechargeOrder = new RechargeOrder();
        rechargeOrder.setPayer(payer);
        rechargeOrder.setAmount(amount);
        rechargeOrder.setCreateTime(LocalDateTime.now());
        return rechargeOrderRepository.save(rechargeOrder);
    }
}
