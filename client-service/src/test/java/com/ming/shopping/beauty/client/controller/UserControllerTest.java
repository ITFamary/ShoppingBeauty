package com.ming.shopping.beauty.client.controller;

import com.ming.shopping.beauty.client.ClientConfigTest;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.model.request.LoginOrRegisterBody;
import com.ming.shopping.beauty.service.utils.Constant;
import me.jiangcai.wx.model.Gender;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author helloztt
 */
public class UserControllerTest extends ClientConfigTest {
    private static final String BASE_URL = "/user";
    @Test
    public void userBaseInfo() throws Exception {
        //造一个未激活的用户
        //先登录，然后看看信息
        LoginOrRegisterBody registerBody = new LoginOrRegisterBody();
        registerBody.setMobile(randomMobile());
        registerBody.setAuthCode("1234");
        registerBody.setSurname(randomChinese(1));
        registerBody.setGender(randomEnum(Gender.class));

        MockHttpSession session = (MockHttpSession)mockMvc.perform(makeWechat(post(Constant.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerBody))))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession();
        mockMvc.perform(makeWechat(get(BASE_URL).session(session))).andDo(print());

    }

}