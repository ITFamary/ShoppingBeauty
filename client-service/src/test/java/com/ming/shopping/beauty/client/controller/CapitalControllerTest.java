package com.ming.shopping.beauty.client.controller;

import com.ming.shopping.beauty.client.ClientConfigTest;
import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.model.request.DepositBody;
import org.junit.Test;
import org.mockito.internal.matchers.Contains;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author helloztt
 */
public class CapitalControllerTest extends ClientConfigTest {
    private static final String BASE_URL = "/capital";
    private static final String DEPOSIT = BASE_URL + "/deposit";
    private static final String FLOW = BASE_URL + "/flow";

    @Test
    public void testCapitalFlow() {
        // TODO: 2018/1/16 等充值、支付订单都搞好了再做这个
    }

    /**
     * 对未激活用户充值，期望：激活用户，余额增加，日志增加
     * 对激活用户充值，期望：余额增加，日志增加
     * 1、充值卡充值
     */
    @Test
    public void testDeposit() throws Exception {
        //先创建一个没激活的用户
        Login mockLogin = mockLogin();
        //登录获取session
        MockHttpSession loginSession = login(mockLogin);

        DepositBody postData = new DepositBody();
        //1、格式错误
        mockMvc.perform(post(DEPOSIT)
                .session(loginSession)
                .header(HttpHeaders.ACCEPT,MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", new Contains("/error?status="+HttpStatusCustom.SC_DATA_NOT_VALIDATE
                        + "&message=")));
        postData.setDepositSum(BigDecimal.ONE);
        mockMvc.perform(post(DEPOSIT)
                .session(loginSession)
                .header(HttpHeaders.ACCEPT,MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("depositSum",postData.getDepositSum().toString()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", new Contains("/error?status="+HttpStatusCustom.SC_DATA_NOT_VALIDATE
                        + "&message=")));
        postData.setDepositSum(null);
        postData.setCdKey("123");
        mockMvc.perform(post(DEPOSIT)
                .session(loginSession)
                .header(HttpHeaders.ACCEPT,MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("cdKey",postData.getCdKey()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", new Contains("/error?status="+HttpStatusCustom.SC_DATA_NOT_VALIDATE
                        + "&message=")));
        //2、错误的充值卡
        postData.setCdKey(String.format("%20d", 0));
        mockMvc.perform(post(DEPOSIT)
                .session(loginSession)
                .header(HttpHeaders.ACCEPT,MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("cdKey",postData.getCdKey()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", new Contains("/error?status="+HttpStatusCustom.SC_DATA_NOT_VALIDATE
                        + "&message=")));
        //3、正确的充值卡
        RechargeCard rechargeCard = mockRechargeCard(null, null);
        postData.setCdKey(rechargeCard.getCode());
        mockMvc.perform(post(DEPOSIT)
                .session(loginSession)
                .header(HttpHeaders.ACCEPT,MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("cdKey",postData.getCdKey()))
                .andExpect(status().isFound());
        mockLogin = loginService.findOne(mockLogin.getId());
        assertThat(mockLogin.getUser().isActive()).isTrue();
        assertThat(mockLogin.getUser().getCurrentAmount().compareTo(rechargeCard.getAmount())).isEqualTo(0);
        //4、已被使用的充值卡
        mockMvc.perform(post(DEPOSIT)
                .session(loginSession)
                .header(HttpHeaders.ACCEPT,MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("cdKey",postData.getCdKey()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", new Contains("/error?status="+HttpStatusCustom.SC_DATA_NOT_VALIDATE
                        + "&message=")));
        //5、查询流水
        mockMvc.perform(get(FLOW)
                .session(loginSession))
                .andDo(print())
                .andExpect(jsonPath("$.pagination.total").value(1))
                .andExpect(jsonPath("$.list[0].sum").value(rechargeCard.getAmount().doubleValue()));
    }

}