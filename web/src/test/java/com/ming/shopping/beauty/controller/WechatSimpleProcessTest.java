package com.ming.shopping.beauty.controller;

import com.jayway.jsonpath.JsonPath;
import com.ming.shopping.beauty.client.controller.CapitalControllerTest;
import com.ming.shopping.beauty.client.controller.ClientItemControllerTest;
import com.ming.shopping.beauty.client.controller.ClientMainOrderControllerTest;
import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.entity.item.RechargeCard_;
import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Represent;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.request.DepositBody;
import com.ming.shopping.beauty.service.model.request.LoginOrRegisterBody;
import com.ming.shopping.beauty.service.repository.RechargeCardRepository;
import com.ming.shopping.beauty.service.service.StagingService;
import com.ming.shopping.beauty.service.service.StoreItemService;
import com.ming.shopping.beauty.service.service.SystemService;
import me.jiangcai.lib.test.matcher.SimpleMatcher;
import me.jiangcai.wx.model.Gender;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.ming.shopping.beauty.client.controller.CapitalControllerTest.DEPOSIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author CJ
 */
//@ActiveProfiles(ServiceConfig.PROFILE_MYSQL)
public class WechatSimpleProcessTest extends TogetherTest {

    @Autowired
    private StagingService stagingService;
    @Autowired
    private StoreItemService storeItemService;
    @Autowired
    private RechargeCardRepository rechargeCardRepository;

    /**
     * @author CJ
     */
    @Test
    public void flow() throws Exception {
        // 1
        Object[] generatingData = stagingService.generateStagingData();
        Represent represent = (Represent) generatingData[2];
        Item[] items = (Item[]) generatingData[3];
        Item okItem = items[0];
        Item[] otherItems = new Item[items.length - 1];
        System.arraycopy(items, 1, otherItems, 0, otherItems.length);

        Login user = mockLogin();
        updateAllRunWith(user);
        mockMvc.perform(
                get("/items")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(ClientItemControllerTest.resultMatcherForItems())
                // item 是一个Collection 必须拥有第一个item 必须不可以拥有其他item
                .andExpect(jsonPath("$.list[*].title").value(new SimpleMatcher<Collection<String>>(
                        names -> {
                            assertThat(names)
                                    .as("必须包含期待的")
                                    .contains(okItem.getName())
                                    .as("必须不包含不期待的")
                                    .doesNotContain(Stream.of(otherItems).map(Item::getName).toArray(String[]::new));
                            return true;
                        }
                )))
        ;
        // 第三
//        先充值
        recharge();
        Number orderId = JsonPath.read(mockMvc.perform(
                get("/user/vipCard")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(jsonPath("$.orderId").isNumber())
                .andExpect(jsonPath("$.qrCode").isString())
                .andReturn().getResponse().getContentAsString(), "$.orderId");
        // 第四
        updateAllRunWith(represent.getLogin());
        mockMvc.perform(
                get("/items")
                        .param("storeId", represent.getStore().getId().toString())
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(ClientItemControllerTest.resultMatcherForItems())
                // item 是一个Collection 必须拥有第一个item 必须不可以拥有其他item
                .andExpect(jsonPath("$.list[*].title").value(new SimpleMatcher<Collection<String>>(
                        names -> {
                            assertThat(names)
                                    .as("只包含特定门店的")
                                    .containsOnly(okItem.getName());
                            return true;
                        }
                )))
        ;
        // 第五 开始下单
        // 我们得获取具体的StoreItem，基于维持测试代码稳定性的需求
        Map<StoreItem, Integer> toBuy = new HashMap<>();
        toBuy.put(fromItem(okItem), 1);
        ClientMainOrderControllerTest.makeOrderFor(mockMvc, null, toBuy, orderId.longValue())
                .andExpect(status().isOk());

        // 第六 完成支付，确保完成支付，要是没钱那么就给他钱
        updateAllRunWith(user);
        CapitalControllerTest.payOrder(mockMvc, null, orderId.longValue(), true, () -> {
            recharge();
            return null;
        })
                .andExpect(status().is2xxSuccessful());

        // 第七

        updateAllRunWith(represent.getLogin());
        mockMvc.perform(
                get("/orders").param("orderType", "STORE")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(jsonPath("$.list[0].orderStatusCode").value(2));

        updateAllRunWith(user);
        mockMvc.perform(
                get("/orders").param("orderType", "MEMBER")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(jsonPath("$.list[0].orderStatusCode").value(2));
    }

    private StoreItem fromItem(Item item) {
        return storeItemService.findByItem(item).get(0);
    }

    private void recharge() throws Exception {
        RechargeCard rechargeCard = mockRechargeCard(null, null);
        DepositBody postData = new DepositBody();
        postData.setCdKey(rechargeCard.getCode());
        mockMvc.perform(post(DEPOSIT)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML)
                .param("cdKey", postData.getCdKey()))
                .andExpect(status().isFound());
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
     * <li>新的session</li>
     * <li>请求 GET /user: 返回 {@link com.ming.shopping.beauty.service.model.HttpStatusCustom#SC_NO_OPENID} {@code 431}</li>
     * <li>请求 GET /auth 进行授权</li>
     * <li>请求 GET /user: 返回200</li>
     * </ol>
     */
    @Test
    public void register() throws Exception {
        Object[] registerData = stagingService.registerStagingData();
        //来一个新的微信用户
        nextCurrentWechatAccount();
        MockHttpSession session = (MockHttpSession) mockMvc.perform(wechatGet("/user"))
                .andExpect(status().is(HttpStatusCustom.SC_NO_OPENID))
                .andReturn().getRequest().getSession();

        //授权去,随便搞个跳转地址
        final String userUrl = "/user", vipUrl = "/user/vipCard", deposit = "/capital/deposit";
        mockMvc.perform(wechatGet(SystemService.AUTH)
                .param("redirectUrl", userUrl)
                .session(session))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", userUrl));

        mockMvc.perform(wechatGet(userUrl)
                .session(session))
                .andExpect(status().is(HttpStatusCustom.SC_NO_USER));

        //普通注册
        LoginOrRegisterBody registerBody = new LoginOrRegisterBody();
        registerBody.setMobile(randomMobile());
        registerBody.setAuthCode("1234");
        registerBody.setSurname(randomChinese(1));
        registerBody.setGender(randomEnum(Gender.class));

        register(registerBody, session);

        mockMvc.perform(wechatGet(userUrl)
                .session(session))
                .andExpect(status().isOk());

        mockMvc.perform(wechatGet(vipUrl)
                .session(session))
                .andExpect(status().is(HttpStatusCustom.SC_DATA_NOT_VALIDATE))
                .andExpect(jsonPath(RESULT_CODE_PATH).value(ResultCodeEnum.USER_NOT_ACTIVE.getCode()));

        //充值卡充值
        //找一张没用的充值卡
        List<RechargeCard> rechargeCard = (List<RechargeCard>) registerData[0];
        String cdKey = rechargeCard.stream().findAny().get().getCode();
        mockMvc.perform(wechatPost(deposit).session(session)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML)
                .param("cdKey", cdKey)).andDo(print())
                .andExpect(status().isFound());
        //这个时候个人中心有数据了
        mockMvc.perform(wechatGet(vipUrl)
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vipCard").value(cdKey));

        //新的session
        session = (MockHttpSession) mockMvc.perform(wechatGet("/user"))
                .andExpect(status().is(HttpStatusCustom.SC_NO_OPENID))
                .andReturn().getRequest().getSession();
        mockMvc.perform(wechatGet(SystemService.AUTH)
                .param("redirectUrl", userUrl)
                .session(session))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", userUrl));
        mockMvc.perform(wechatGet(userUrl)
                .session(session))
                .andExpect(status().isOk());
    }

    private void register(LoginOrRegisterBody registerBody, MockHttpSession session) throws Exception {
        mockMvc.perform(wechatPost(SystemService.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerBody))
                .session(session))
                .andExpect(status().isOk());
    }


}
