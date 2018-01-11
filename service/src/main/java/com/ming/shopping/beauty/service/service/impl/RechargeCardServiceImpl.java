package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.aop.BusinessSafe;
import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.entity.item.RechargeCard_;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.repository.RechargeCardRepository;
import com.ming.shopping.beauty.service.repository.UserRepository;
import com.ming.shopping.beauty.service.service.RechargeCardService;
import me.jiangcai.lib.sys.service.SystemStringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by helloztt on 2018/1/4.
 */
@Service
public class RechargeCardServiceImpl implements RechargeCardService {
    @Autowired
    private RechargeCardRepository rechargeCardRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SystemStringService systemStringService;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public List<RechargeCard> newCard(int num, Login guide, Login manage) {
        List<RechargeCard> cardList = new ArrayList<>(num);
        RechargeCard rechargeCard = new RechargeCard();
        rechargeCard.setGuideUser(guide);
        rechargeCard.setManager(manage);
        rechargeCard.setCreateTime(LocalDateTime.now());
        Integer defaultAmount = systemStringService.getCustomSystemString("shopping.service.card.amount", null, true, Integer.class, 500);
        rechargeCard.setAmount(BigDecimal.valueOf(defaultAmount));
        for (int i = 0; i < num; i++) {
            cardList.add((RechargeCard) rechargeCard.clone());
        }
        rechargeCardRepository.save(cardList);
        rechargeCardRepository.flush();
        // TODO: 2018/1/12 由于卡密的生成方式还不确定，目前暂时格式化id来作为卡密
        cardList.forEach(card -> {
            card.setCode(String.format("%20d", card.getId()));
        });
        rechargeCardRepository.save(cardList);
        return cardList;
    }

    @Override
    @Transactional(readOnly = true)
    public RechargeCard verify(String cardNo) {
        // TODO: 2018/1/5
        RechargeCard rechargeCard = rechargeCardRepository.findOne((root, cq, cb)
                -> cb.equal(root.get(RechargeCard_.code), cardNo));
        if (rechargeCard == null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.CARD_NOT_EXIST));
        }
        if (rechargeCard.isUsed()) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.CARD_ALREADY_USED));
        }
        return rechargeCard;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    @BusinessSafe
    public void useCard(String cardNo, Long userId) {
        RechargeCard rechargeCard = verify(cardNo);
        User user = userRepository.findOne(userId);
        if (user == null || !user.getLogin().isEnabled()) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.LOGIN_NOT_EXIST));
        }
        rechargeCard.setUsed(true);
        rechargeCard.setUser(user);

        user.setCurrentAmount(user.getCurrentAmount().add(rechargeCard.getAmount()));
        if (!user.isActive()) {
            user.setActive(true);
        }
        rechargeCardRepository.save(rechargeCard);
        userRepository.save(user);

        // TODO: 2018/1/6 直接从充值卡读取用户的充值记录即可？不需要另外加表记录充值日志了？
    }
}
