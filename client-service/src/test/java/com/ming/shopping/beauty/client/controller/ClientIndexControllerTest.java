package com.ming.shopping.beauty.client.controller;


import com.ming.shopping.beauty.client.ClientConfigTest;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import me.jiangcai.wx.model.WeixinUserDetail;
import me.jiangcai.wx.test.WeixinTestConfig;
import me.jiangcai.wx.web.exception.NoWeixinClientException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author helloztt
 */
public class ClientIndexControllerTest extends ClientConfigTest {

    @Test
    public void isExistTest() throws Exception{
        final String isExistUrl = "/isExist";
//        //非微信环境
        try{
            mockMvc.perform(get(isExistUrl))
                    .andExpect(status().isOk())
                    .andDo(print());
            assertThat(true).isFalse();
        }catch (Exception ex){
            assertThat(ex.getMessage()).contains("NoWeixinClientException");
        }

        //没注册时，期望返回空数据
        mockMvc.perform(makeWechat(get(isExistUrl)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(RESULT_CODE_PATH).value(HttpStatusCustom.SC_OK))
                .andExpect(jsonPath(RESULT_DATA_PATH).isEmpty());
    }

}