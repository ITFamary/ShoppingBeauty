package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.User;

import java.util.List;

/**
 * @author helloztt
 */
public interface RechargeCardService {

    /**
     * 新增卡密
     * @param guideId 推荐人
     * @param manageId 操作员
     * @return
     */
    RechargeCard newCard(Long guideId, Long manageId);

    /**
     * 批量新增卡密
     *
     * @param num    数量
     * @param guideId  推荐人
     * @param manageId 操作员
     * @return
     */
    List<RechargeCard> newCard(int num, Long guideId, Long manageId);

    /**
     * 校验卡密
     *
     * @param cardNo
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
