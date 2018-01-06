package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.User;

/**
 * Created by helloztt on 2018/1/4.
 */
public interface RechargeCardService {

    /**
     * 校验卡密
     * @param cardNo
     */
    RechargeCard verify(String cardNo);

    /**
     * 使用卡密，加上业务锁
     * 实现中应该有这样几个步骤：
     * 1.校验，如果错误就抛出异常
     * 2.设置充值卡已被谁使用
     * 3.给这个用户激活，增加金额和充值流水
     * @param cardNo 卡密
     * @param userId 充值的用户
     */
    void useCard(String cardNo, Long userId);
}
