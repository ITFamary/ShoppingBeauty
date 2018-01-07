package com.ming.shopping.beauty.service.service;

import com.huotu.verification.VerificationType;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.utils.Constant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.jiangcai.lib.notice.Content;
import me.jiangcai.wx.model.Gender;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by helloztt on 2018/1/4.
 */
public interface LoginService {

    /**
     * 根据openId 查找用户，如果查到了就返回这个用户，如果没查到就创建用户
     * 创建用户，如果有卡密，那就激活这个用户
     *
     * @param openId      微信唯一标示
     * @param mobile      手机号
     * @param verifyCode  验证码
     * @param familyName  姓
     * @param gender      性别
     * @param cardNo      卡密
     * @param guideUserId 引导者
     * @return
     */
    Login getLogin(String openId, String mobile, String verifyCode
            , String familyName, Gender gender, String cardNo, Long guideUserId);

    /**
     * 根据openId 查找角色
     *
     * @param openId
     * @return
     */
    Login asWechat(String openId);

    /**
     * 查找角色，并校验角色是否可用，若不可用则抛出异常
     *
     * @param id
     * @return
     * @throws ApiResultException 校验失败返回结果
     */
    Login findOne(long id) throws ApiResultException;

    /**
     * 冻结或启用用户
     *
     * @param id
     * @param enable 是否启用
     */
    @Transactional(rollbackFor = RuntimeException.class)
    void freezeOrEnable(long id, boolean enable);

    /**
     * @return 用于登录的验证码
     */
    default VerificationType loginVerificationType() {
        return new VerificationType() {
            @Override
            public int id() {
                return 1;
            }

            @Override
            public boolean allowMultiple() {
                return true;
            }

            @Override
            public String message(String code) {
                return "登录短信验证码为：" + code + "；请勿泄露。";
            }

            @Override
            public Content generateContent(String code) {
                return Constant.generateCodeContent(this, code, "SMS_94310019");
            }
        };
    }

    /**
     * @return 用于注册的验证码
     */
    default VerificationType registerVerificationType() {
        return new VerificationType() {
            @Override
            public int id() {
                return 2;
            }

            @Override
            public boolean allowMultiple() {
                return true;
            }

            @Override
            public String message(String code) {
                return "注册短信验证码为：" + code + "；请勿泄露。";
            }

            @Override
            public Content generateContent(String code) {
                return Constant.generateCodeContent(this, code, "SMS_94310017");
            }
        };
    }

    @AllArgsConstructor
    @Getter
    enum ErrorMessage{
        LOGIN_NOT_EXIST("账号不存在"),
        LOGIN_NOT_ENABLE("账号不可用");

        private String message;


    }
}
