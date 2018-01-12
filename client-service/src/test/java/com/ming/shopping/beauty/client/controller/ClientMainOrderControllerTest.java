package com.ming.shopping.beauty.client.controller;

import com.ming.shopping.beauty.client.ClientConfigTest;
import com.ming.shopping.beauty.service.entity.login.Login;
import org.junit.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author lxf
 */
public class ClientMainOrderControllerTest extends ClientConfigTest {
    @Test
    public void go() throws Exception {

        //激活的用户获取订单列表 懵了
        mockMvc.perform(get("/orders")
                .session(activeUserSession))
                .andExpect(status().isOk());
    }
}

