package com.ming.shopping.beauty.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ming.shopping.beauty.client.controller.ClientItemControllerTest;
import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.request.LoginOrRegisterBody;
import com.ming.shopping.beauty.service.service.StagingService;
import com.ming.shopping.beauty.service.service.SystemService;
import me.jiangcai.wx.model.Gender;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * <ol>
 * <li>构建商户，门店，项目以及门店项目；还有门店代表 应该在main代码中，因为staging也需要自己完成这么一份数据</li>
 * <li>用户访问/items 可以获得门店项目；而不是项目，应该校验结果是否仅仅在项目中或者未被激活 还有格式检测</li>
 * <li>用户访问/users/vipCard 可以获得订单号；以及被扫码的地址</li>
 * <li>门店代表 可以根据自己的storeId 访问/items 获取他们自己的门店项目</li>
 * <li>门店代表 可以通过post /order 完成下单</li>
 * <li>用户 可以通过 PUT /capital/payment/{orderId} 完成支付</li>
 * <li>门店代表 可以通过 get /orders 获得该用户已支付的信息</li>
 * </ol>
 *
 * @author CJ
 */
public class WechatSimpleProcessTest extends TogetherTest {

    @Autowired
    private StagingService stagingService;

    @Test
    public void flow() throws Exception {
        // 1
        Object[] generatingData = stagingService.generateStagingData();

        Login user = mockLogin();
        updateAllRunWith(user);
        mockMvc.perform(
                get("/items")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(ClientItemControllerTest.isItemsResponse())
        // 然后我需要确认此处生成的数据并不包含
        ;
    }

    /**
     * 注册流程
     * <p>
     * 未注册用户从公众号-个人中心进入：
     * <ol>
     * <li>请求 GET /user: 返回 {@link com.ming.shopping.beauty.service.model.HttpStatusCustom#SC_NO_OPENID} {@code 431}</li>
     * <li>请求 GET /auth 进行授权</li>
     * <li>请求 GET /user: 返回 401 没有权限</li>
     * <li>请求 POST /auth: 返回200 成功</li>
     * <li>请求 GET /user: 返回200（这个时候用户还没被激活）</li>
     * <li>请求 GET /user/vipCard: 返回{@link com.ming.shopping.beauty.service.model.HttpStatusCustom#SC_DATA_NOT_VALIDATE}210，resultCode={@link com.ming.shopping.beauty.service.model.ResultCodeEnum#USER_NOT_ACTIVE} {@code 2005}</li>
     * <li>请求 POST /capital/deposit: 充值成功，跳转到目标页面</li>
     * <li>请求 GET /user/vipCard：返回200</li>
     * </ol>
     */
    @Test
    public void register() throws Exception {
        //来一个新的微信用户
        nextCurrentWechatAccount();
        MockHttpSession session = (MockHttpSession) mockMvc.perform(wechatGet("/user"))
                .andExpect(status().is(HttpStatusCustom.SC_NO_OPENID))
                .andReturn().getRequest().getSession();

        //授权去,随便搞个跳转地址
        final String userUrl = "/user",vipUrl = "/user/vipCard";
        mockMvc.perform(wechatGet(SystemService.AUTH)
                .param("redirectUrl",userUrl)
                .session(session))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",userUrl));

        mockMvc.perform(wechatGet(userUrl)
                .session(session))
                .andExpect(status().is(HttpStatusCustom.SC_NO_USER));

        //普通注册
        LoginOrRegisterBody registerBody = new LoginOrRegisterBody();
        registerBody.setMobile(randomMobile());
        registerBody.setAuthCode("1234");
        registerBody.setSurname(randomChinese(1));
        registerBody.setGender(randomEnum(Gender.class));

        register(registerBody,session);

        mockMvc.perform(wechatGet(userUrl)
                .session(session))
                .andExpect(status().isOk());

        mockMvc.perform(wechatGet(vipUrl)
                .session(session))
                .andExpect(status().is(HttpStatusCustom.SC_DATA_NOT_VALIDATE))
                .andExpect(jsonPath(RESULT_CODE_PATH).value(ResultCodeEnum.USER_NOT_ACTIVE.getCode()));
        // TODO: 2018-02-10 充值卡测试

    }

    private void register(LoginOrRegisterBody registerBody,MockHttpSession session) throws Exception {
        mockMvc.perform(wechatPost(SystemService.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerBody))
                .session(session))
                .andExpect(status().isOk());
    }



}
