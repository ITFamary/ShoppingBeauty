package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.ManageConfigTest;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.model.request.LoginOrRegisterBody;
import com.ming.shopping.beauty.service.service.InitService;
import com.ming.shopping.beauty.service.service.SystemService;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author helloztt
 */
public class ManageControllerTest extends ManageConfigTest {
    private static final String manageLogin = "/managerLogin", managerLoginRequest = "/managerLoginRequest", manageLoginResult = "/manageLoginResult";

    @Test
    public void testManageIndex() throws Exception {
        //非微信环境
        try {
            mockMvc.perform(get(manageLogin+ "/" + UUID.randomUUID().toString()))
                    .andExpect(status().isOk());
            assertThat(true).isFalse();
        } catch (Exception ex) {
            assertThat(ex.getMessage()).contains("NoWeixinClientException");
        }
        nextCurrentWechatAccount();
        //以CJ的超级管理员账号为测试对象
        //随便找个ID登录，期望提示session失效
        mockMvc.perform(wechatGet(manageLogin + "/" + UUID.randomUUID().toString()))
                .andExpect(status().is(HttpStatusCustom.SC_SESSION_TIMEOUT));
        //去获取一个ID
        MvcResult mvcResult = mockMvc.perform(wechatGet(managerLoginRequest))
                .andExpect(status().is(HttpStatusCustom.SC_ACCEPTED))
                .andReturn();
        //之后判断登录状态需要用这个session
        MockHttpSession session = (MockHttpSession) mvcResult.getRequest().getSession();
        String result = mvcResult.getResponse().getContentAsString();
        String sessionId = objectMapper.readTree(result).get("id").asText();
        //再次登录，由于CJ的账号没有openId，期望 HttpStatusCustom.SC_LOGIN_NOT_EXIST
        mockMvc.perform(wechatGet(manageLogin + "/" + sessionId))
                .andExpect(status().is(HttpStatusCustom.SC_LOGIN_NOT_EXIST));

        //用蒋才的账号登录一下，更新openId
        LoginOrRegisterBody registerBody = new LoginOrRegisterBody();
        registerBody.setMobile(InitService.cjMobile);
        registerBody.setAuthCode("1234");
        mockMvc.perform(makeWechat(post(SystemService.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerBody))))
                .andExpect(status().isOk());
        //登录前看看登录情况
        mockMvc.perform(get(manageLoginResult)
                .session(session))
                .andExpect(status().isNoContent());

        //再次登录，期望成功
        mockMvc.perform(wechatGet(manageLogin + "/" + sessionId))
                .andExpect(status().isOk());

        //看看登录结果
        mockMvc.perform(get(manageLoginResult)
                .session(session))
                .andExpect(status().isOk());

        //再试一个没有权限的用户
        Login login = mockLogin();
        mvcResult = mockMvc.perform(wechatGet(managerLoginRequest))
                .andExpect(status().is(HttpStatusCustom.SC_ACCEPTED))
                .andReturn();
        session = (MockHttpSession) mvcResult.getRequest().getSession();
        result = mvcResult.getResponse().getContentAsString();
        sessionId = objectMapper.readTree(result).get("id").asText();
        nextCurrentWechatAccount(login.getWechatUser());

        mockMvc.perform(get(manageLoginResult).session(session)).andExpect(status().isNoContent());

        mockMvc.perform(wechatGet(manageLogin + "/" + sessionId))
                .andExpect(status().isForbidden());

        mockMvc.perform(get(manageLoginResult).session(session)).andExpect(status().is(HttpStatusCustom.SC_SESSION_TIMEOUT));
    }

}