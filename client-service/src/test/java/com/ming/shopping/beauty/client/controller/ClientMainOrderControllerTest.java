package com.ming.shopping.beauty.client.controller;

import com.ming.shopping.beauty.client.ClientConfigTest;
import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.entity.login.*;
import com.ming.shopping.beauty.service.model.request.OrderSearcherBody;
import org.junit.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author lxf
 */
public class ClientMainOrderControllerTest extends ClientConfigTest {
    @Test
    public void go() throws Exception {
        //先看看没数据的订单长啥样
        OrderSearcherBody searcherBody = new OrderSearcherBody();
        mockMvc.perform(get("/orders")
            .session(activeUserSession)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(searcherBody)))
            .andDo(print())
            .andExpect(status().isOk());

        //造订单啦~
        //首先要有 商户，门店，项目，门店项目
        Merchant mockMerchant = mockMerchant();
        Store mockStore = mockStore(mockMerchant);
        for (int i = 0; i < 5; i++) {
            Item item = mockItem(mockMerchant);
            mockStoreItem(mockStore, item);
        }

        //然后需要有下单的用户(mockActiveUser)和门店代表
        Represent mockRepresent = mockRepresent(mockStore);

        //来它至少2个订单
        for (int i = 0; i < 2 + random.nextInt(5); i++) {
            mockMainOrder(mockActiveUser.getUser(), mockRepresent);
        }


        //激活的用户获取订单列表 懵了
        mockMvc.perform(get("/orders")
                .session(activeUserSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searcherBody)))
                .andDo(print())
                .andExpect(status().isOk());
    }
}

