package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.ManageConfigTest;
import org.junit.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ManageMerchantControllerTest extends ManageConfigTest {

    @Test
    public void go() throws Exception {

        mockMvc.perform(get("/merchant")).andDo(print()).andExpect(status().isOk());
    }
}
