package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.entity.order.RechargeOrder;

import java.math.BigDecimal;

/**
 * @author helloztt
 */
public interface RechargeOrderService {
    /**
     * 新增充值订单
     *
     * @param payer  下单用户
     * @param amount 下单金额
     * @return
     */
    RechargeOrder newOrder(User payer, BigDecimal amount);
}
