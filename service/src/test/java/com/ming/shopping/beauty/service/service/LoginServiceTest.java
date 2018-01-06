package com.ming.shopping.beauty.service.service;

import com.huotu.verification.service.VerificationCodeService;
import com.ming.shopping.beauty.service.CoreServiceTest;
import com.ming.shopping.beauty.service.entity.login.Login;
import me.jiangcai.wx.model.Gender;
import me.jiangcai.wx.model.WeixinUserDetail;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * Created by helloztt on 2018/1/5.
 */
public class LoginServiceTest extends CoreServiceTest {
    @Autowired
    private VerificationCodeService verificationCodeService;
    @Autowired
    private LoginService loginService;
    @Test
    public void getLogin() throws Exception {
        WeixinUserDetail weixinUserDetail = nextCurrentWechatAccount();
        String mobile = randomMobile();
        verificationCodeService.sendCode(mobile,loginService.loginVerificationType());
        Login login = loginService.getLogin(weixinUserDetail.getOpenId(),mobile,
                "1234",randomString(2), Gender.female,null,null);
        assertThat(login).isNotNull();
        assertThat(login.getUser()).isNotNull();
        assertThat(login.getUser().isActive()).isFalse();
    }

}