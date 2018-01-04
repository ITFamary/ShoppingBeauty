package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.aop.BusinessSafe;
import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.repository.RechargeCardRepository;
import com.ming.shopping.beauty.service.service.RechargeCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Created by helloztt on 2018/1/4.
 */
@Service
public class RechargeCardServiceImpl implements RechargeCardService {
    @Autowired
    private RechargeCardRepository rechargeCardRepository;

    @Override
    @Transactional(readOnly = true)
    public void verify(String cardNo) {
        // TODO: 2018/1/5
        RechargeCard rechargeCard = rechargeCardRepository.findOne((root, cq, cb)
                -> null);
        if (rechargeCard == null)
            throw new ApiResultException(ApiResult.withError(HttpStatusCustom.SC_EXPECTATION_FAILED, "充值卡无效"));
        if (rechargeCard.isUsed())
            throw new ApiResultException(ApiResult.withError(HttpStatusCustom.SC_EXPECTATION_FAILED, "充值卡已失效"));
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    @BusinessSafe
    public void useCard(String cardNo, Login user) {
        verify(cardNo);
    }
}
