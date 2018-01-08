package com.ming.shopping.beauty.client;

import com.ming.shopping.beauty.client.config.ClientConfig;
import com.ming.shopping.beauty.service.CoreServiceTest;
import me.jiangcai.wx.model.WeixinUserDetail;
import org.junit.Before;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author helloztt
 */
@ContextConfiguration(classes = ClientConfig.class)
public abstract class ClientConfigTest extends CoreServiceTest {

    protected WeixinUserDetail mockWeixinUser;

    @Before
    public void setupByClient(){
        mockWeixinUser = nextCurrentWechatAccount();
    }
}
