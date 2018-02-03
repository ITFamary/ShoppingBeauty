package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.ManageConfigTest;
import com.ming.shopping.beauty.service.entity.login.Login;
import org.hamcrest.core.StringStartsWith;
import org.junit.Test;

import java.math.BigDecimal;

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
                .andExpect(jsonPath("$.id").value(mockLogin.getId()))
                .andExpect(jsonPath("$.name").value(StringStartsWith.startsWith(mockLogin.getUser().getFamilyName())))
                .andExpect(jsonPath("$.mobile").value(mockLogin.getLoginName()))
                .andExpect(jsonPath("$.enabled").value(mockLogin.isEnabled()))
                .andExpect(jsonPath("$.balance").value(BigDecimal.ZERO))
                .andExpect(jsonPath("$.consumption").value(BigDecimal.ZERO));
    }

    @Test
    public void setEnable() throws Exception {
    }

}