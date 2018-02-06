package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.ManageConfigTest;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.repository.LoginRepository;
import org.hamcrest.core.StringStartsWith;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author helloztt
 */
public class ManageLoginControllerTest extends ManageConfigTest {
    public static final String BASE_URL = "/login";

    @Autowired
    private LoginRepository loginRepository;
    @Test
    public void getOne() throws Exception {
        //来个管理员
        Login rootLogin = mockRoot();
        updateAllRunWith(rootLogin);

        Login mockLogin = mockLogin();
        mockMvc.perform(get(BASE_URL + "/" + mockLogin.getId()))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id").value(mockLogin.getId()))
                .andExpect(jsonPath("$.name").value(StringStartsWith.startsWith(mockLogin.getUser().getFamilyName())))
                .andExpect(jsonPath("$.mobile").value(mockLogin.getLoginName()))
                .andExpect(jsonPath("$.enabled").value(mockLogin.isEnabled()))
                .andExpect(jsonPath("$.balance").value(BigDecimal.ZERO))
                .andExpect(jsonPath("$.consumption").value(BigDecimal.ZERO));
    }

    @Test
    public void setGuidable() throws Exception {
        Login login = mockLogin();
        Login root = mockRoot();
        updateAllRunWith(root);
        //可以推荐他人
        mockMvc.perform(put("/login/"+login.getId()+"/guidable")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(true)))
                .andDo(print())
                .andReturn();
        Login one = loginRepository.findOne(login.getId());
        //一定是禁用的
        assertThat(one.isGuidable()).isTrue();
        //启用
        mockMvc.perform(put("/login/"+login.getId()+"/guidable")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(false)))
                .andDo(print())
                .andReturn();
        Login two = loginRepository.findOne(login.getId());
        //一定是启用的
        assertThat(two.isGuidable()).isFalse();
    }

    @Test
    public void setEnable() throws Exception {
        Login login = mockLogin();
        Login root = mockRoot();
        updateAllRunWith(root);
        //禁用
        mockMvc.perform(put("/login/"+login.getId()+"/enabled")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(false)))
                .andDo(print())
                .andReturn();
        Login one = loginRepository.findOne(login.getId());
        //一定是禁用的
        assertThat(one.isEnabled()).isFalse();
        //启用
        mockMvc.perform(put("/login/"+login.getId()+"/enabled")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(true)))
                .andDo(print())
                .andReturn();
        Login two = loginRepository.findOne(login.getId());
        //一定是启用的
        assertThat(two.isEnabled()).isTrue();
    }

}