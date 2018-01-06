package com.ming.shopping.beauty.service.service.impl;

import com.huotu.verification.IllegalVerificationCodeException;
import com.huotu.verification.service.VerificationCodeService;
import com.ming.shopping.beauty.service.aop.BusinessSafe;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Login_;
import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.repository.LoginRepository;
import com.ming.shopping.beauty.service.repository.UserRepository;
import com.ming.shopping.beauty.service.service.LoginService;
import com.ming.shopping.beauty.service.service.RechargeCardService;
import me.jiangcai.wx.model.Gender;
import me.jiangcai.wx.standard.repository.StandardWeixinUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * Created by helloztt on 2018/1/4.
 */
@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private LoginRepository loginRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VerificationCodeService verificationCodeService;
    @Autowired
    private RechargeCardService rechargeCardService;
    @Autowired
    private StandardWeixinUserRepository standardWeixinUserRepository;
    @Autowired
    private Environment env;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    @BusinessSafe
    public Login getLogin(String openId, String mobile, String verifyCode
            , String familyName, Gender gender, String cardNo, Long guideUserId) {
        if (!StringUtils.isEmpty(verifyCode)) {
            verificationCodeService.verify(mobile, verifyCode, loginVerificationType());
        }
        if (!StringUtils.isEmpty(cardNo)) {
            rechargeCardService.verify(cardNo);
        }
        Login login = asWechat(openId);
        if (login != null) {
            return login;
        }
        login = new Login();
        login.setLoginName(mobile);
        login.setWechatUser(standardWeixinUserRepository.findByOpenId(openId));
        login.setCreateTime(LocalDateTime.now());
        loginRepository.saveAndFlush(login);
        User user = new User();
        login.setUser(user);
        user.setId(login.getId());
        user.setLogin(login);
        user.setFamilyName(familyName);
        user.setGender(gender);
        if (guideUserId != null && guideUserId > 0) {
            user.setGuideUser(loginRepository.findOne(guideUserId));
        }
        if (!StringUtils.isEmpty(cardNo)) {
            //使用这张充值卡，如果不存在或者已经用过了，就抛出异常
            rechargeCardService.useCard(cardNo, login.getId());
            user.setCardNo(cardNo);
            user.setActive(true);
        }
        userRepository.saveAndFlush(user);
        return login;
    }

    @Override
    public Login asWechat(String openId) {
        // TODO: 2018/1/5 openId -》 _.openId
        return loginRepository.findOne((root, query, cb)
                -> cb.equal(root.get(Login_.wechatUser).get("openId"), openId)
        );
    }


}
