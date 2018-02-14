package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.business.RechargeCardBatch;
import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.entity.login.Login;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author helloztt
 */
public interface RechargeCardService {

    /**
     * @param operator     操作者；可以为null
     * @param guideId      发展者
     * @param emailAddress 发展者的email地址；它可以接收到卡密信息
     * @param num          数量
     * @return 批次
     */
    @Transactional
    RechargeCardBatch newBatch(Login operator, long guideId, String emailAddress, int num);

    /**
     * 校验卡密
     *
     * @param cardNo
     * @return
     */
    RechargeCard verify(String cardNo);

    /**
     * 使用卡密，加上业务锁
     * 实现中应该有这样几个步骤：
     * 1.校验，如果错误就抛出异常
     * 2.设置充值卡已被谁使用
     * 3.给这个用户激活，增加金额
     *
     * @param cardNo 卡密
     * @param userId 充值的用户
     */
    void useCard(String cardNo, Long userId);
}
