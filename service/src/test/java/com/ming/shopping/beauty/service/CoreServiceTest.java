package com.ming.shopping.beauty.service;

import com.ming.shopping.beauty.service.config.ServiceConfig;
import com.ming.shopping.beauty.service.repository.MerchantRepository;
import me.jiangcai.lib.test.SpringWebTest;
import me.jiangcai.wx.model.WeixinUserDetail;
import me.jiangcai.wx.test.WeixinTestConfig;
import me.jiangcai.wx.test.WeixinUserMocker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * @author helloztt
 */
@ActiveProfiles({ServiceConfig.PROFILE_TEST, ServiceConfig.PROFILE_UNIT_TEST})
@ContextConfiguration(classes = {CoreServiceTestConfig.class})
@WebAppConfiguration
public abstract class CoreServiceTest extends SpringWebTest {
    @Autowired
    protected WeixinTestConfig weixinTestConfig;
    @Autowired
    protected MerchantRepository merchantRepository;

    /**
     * @return 生成一个新的微信帐号，并且应用在系统中
     */
    protected WeixinUserDetail nextCurrentWechatAccount() {
        WeixinUserDetail detail = WeixinUserMocker.randomWeixinUserDetail();
        weixinTestConfig.setNextDetail(detail);
        return detail;
    }
}
