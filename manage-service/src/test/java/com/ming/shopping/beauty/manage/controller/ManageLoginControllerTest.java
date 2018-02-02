package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.ManageConfigTest;
import com.ming.shopping.beauty.service.entity.login.Login;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author helloztt
 */
public class ManageLoginControllerTest extends ManageConfigTest {
    public static final String BASE_URL = "/login";
    @Test
    public void getOne() throws Exception {
        //来个管理员
        Login rootLogin = mockRoot();
        updateAllRunWith(rootLogin);

        Login mockLogin = mockLogin();
        mockMvc.perform(get(BASE_URL + "/" + mockLogin.getId()))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.loginId").value(mockLogin.getId()))
                .andExpect(jsonPath("$.username").value(mockLogin.getUsername()))
                .andExpect(jsonPath("$.mobile").value(mockLogin.getLoginName()))
                .andExpect(jsonPath("$.enabled").value(mockLogin.isEnabled()));
    }

    @Test
    public void setEnable() throws Exception {
    }

}