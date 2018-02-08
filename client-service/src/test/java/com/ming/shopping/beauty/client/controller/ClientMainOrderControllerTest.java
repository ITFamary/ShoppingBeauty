package com.ming.shopping.beauty.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.ming.shopping.beauty.client.ClientConfigTest;
import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.entity.login.*;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.support.OrderStatus;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.request.NewOrderBody;
import com.ming.shopping.beauty.service.model.request.OrderSearcherBody;
import com.ming.shopping.beauty.service.model.request.StoreItemNum;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author lxf
 */
public class ClientMainOrderControllerTest extends ClientConfigTest {
    @Test
    public void go() throws Exception {
        //先看看没数据的订单长啥样
        OrderSearcherBody searcherBody = new OrderSearcherBody();
        String response = mockMvc.perform(get("/orders")
            .session(activeUserSession)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(searcherBody)))
            .andDo(print())
            .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        int total = objectMapper.readTree(response).get("pagination").get("total").asInt();

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
        //以门店身份登录
        MockHttpSession representSession = login(mockRepresent.getLogin());

        //来它至少2个订单
        int randomOrderNum = 2 + random.nextInt(5);
        for (int i = 0; i < randomOrderNum; i++) {
            //先获取用户的 orderId
            response = mockMvc.perform(get("/user/vipCard")
                    .session(activeUserSession))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            long orderId = objectMapper.readTree(response).get("orderId").asLong();
            NewOrderBody orderBody = new NewOrderBody();
            orderBody.setOrderId(orderId);
            Map<StoreItem, Integer> randomMap = randomOrderItemSet(mockStore.getId());
            StoreItemNum[] items = randomMap.keySet().stream().map(p->new StoreItemNum(p.getId(),randomMap.get(p))).toArray(StoreItemNum[]::new);
            orderBody.setItems(items);

            mockMvc.perform(post("/order")
                    .session(representSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(orderBody)))
                    .andExpect(status().isOk());
            MainOrder mainOrder = mainOrderService.findById(orderId);
            assertThat(mainOrder.getOrderStatus().toString()).isEqualTo(OrderStatus.forPay.toString());
            assertThat(mainOrder.getOrderItemList()).isNotEmpty();
            //再次下单会提示错误
            mockMvc.perform(post("/order")
                    .session(representSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(orderBody)))
                    .andExpect(status().is(HttpStatusCustom.SC_DATA_NOT_VALIDATE))
                    .andExpect(jsonPath(RESULT_CODE_PATH).value(ResultCodeEnum.ORDER_NOT_EMPTY.getCode()));
        }


        //激活的用户获取订单列表
        response = mockMvc.perform(get("/orders")
                .session(activeUserSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searcherBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        int totalOrderNum = objectMapper.readTree(response).get("pagination").get("total").asInt();
        assertThat(totalOrderNum).isEqualTo(total + randomOrderNum);
        //获取第一个订单编号
        JsonNode orderList = objectMapper.readTree(response).get("list");
        for(JsonNode order:orderList){
            long orderId = order.get("orderId").asLong();
            //根据这个订单编号查
            mockMvc.perform(get("/orders/" + orderId)
                    .session(activeUserSession))
                    .andDo(print())
                    .andExpect(jsonPath("$.orderId").value(orderId));
        }
    }
}

