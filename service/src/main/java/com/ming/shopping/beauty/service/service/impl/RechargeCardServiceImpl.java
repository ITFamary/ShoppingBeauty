package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.aop.BusinessSafe;
import com.ming.shopping.beauty.service.entity.business.RechargeCardBatch;
import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.entity.item.RechargeCard_;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.repository.LoginRepository;
import com.ming.shopping.beauty.service.repository.RechargeCardBatchRepository;
import com.ming.shopping.beauty.service.repository.RechargeCardRepository;
import com.ming.shopping.beauty.service.repository.UserRepository;
import com.ming.shopping.beauty.service.service.RechargeCardService;
import com.ming.shopping.beauty.service.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private LoginRepository loginRepository;
    @Autowired
    private RechargeCardBatchRepository rechargeCardBatchRepository;
    @Autowired
    private SystemService systemService;

    @Override
    public RechargeCardBatch newBatch(Login operator, long guideId, String emailAddress, int num) {
        RechargeCardBatch batch = new RechargeCardBatch();
        batch.setManager(operator);
        batch.setCreateTime(LocalDateTime.now());
        batch.setGuideUser(loginRepository.getOne(guideId));
        batch.setEmailAddress(emailAddress);

        batch = rechargeCardBatchRepository.save(batch);
        Integer defaultAmount = systemService.currentCardAmount();
        // 生成特定数量的卡密
        batch.setCardSet(newCardSet(batch, num, defaultAmount));

        sendToUser(batch);

        return batch;
    }

    private void sendToUser(RechargeCardBatch batch) {
        // TODO 发送给用户
    }

    private Set<RechargeCard> newCardSet(RechargeCardBatch batch, int num, Integer amount) {
        Stream.Builder<RechargeCard> builder = Stream.builder();
        while (num-- > 0)
            builder = builder.add(new RechargeCard());

        return builder.build()
                .peek(rechargeCard -> {
                    rechargeCard.setBatch(batch);
                    rechargeCard.setAmount(new BigDecimal(amount));
                    rechargeCard.setCode(User.makeCardNo());
                })
                .map(rechargeCardRepository::save)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public RechargeCard verify(String cardNo) {
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
        rechargeCard.setUsedTime(LocalDateTime.now());

        if (!user.isActive()) {
            user.setCardNo(cardNo);
        }
        rechargeCardRepository.saveAndFlush(rechargeCard);
        userRepository.save(user);
    }
}
