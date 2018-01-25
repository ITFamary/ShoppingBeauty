package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.User;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 用户资产管理服务
 * @author lxf
 */
public interface CapitalService {

    /**
     * 手动充值
     *
     * @param manage  操作管理员
     * @param user  充值的用户手机号
     * @param amount  充值的金额
     */
    @Transactional
    void manualRecharge(Login manage, User user, BigDecimal amount);
}
