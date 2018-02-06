package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.ManageConfigTest;
import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Represent;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.support.OrderStatus;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.request.NewOrderBody;
import com.ming.shopping.beauty.service.model.request.StoreItemNum;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author lxf
 */
public class ManageSettlementSheetControllerTest extends ManageConfigTest {

    @Test
    public void go()throws Exception{
        /*//商户
        Merchant merchant = mockMerchant();
        //门店
        Store store = mockStore(merchant);
        for (int i = 0; i < 5; i++) {
            Item item = mockItem(merchant);
            mockStoreItem(store, item);
        }
        //推荐者
        Represent mockRepresent = mockRepresent(store);
        //以门店身份登录
        updateAllRunWith(mockRepresent.getLogin());

        //来它至少2个订单
        int randomOrderNum = 2 + random.nextInt(5);
        for (int i = 0; i < randomOrderNum; i++) {
            //先获取用户的 orderId
            long orderId = Long.valueOf(mockMvc.perform(get("/user/vipCard"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getHeader("X-Order-Id"));
            NewOrderBody orderBody = new NewOrderBody();
            orderBody.setOrderId(orderId);
            Map<StoreItem, Integer> randomMap = randomOrderItemSet(store.getId());
            StoreItemNum[] items = randomMap.keySet().stream().map(p->new StoreItemNum(p.getId(),randomMap.get(p))).toArray(StoreItemNum[]::new);
            orderBody.setItems(items);

            mockMvc.perform(post("/order")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(orderBody)))
                    .andExpect(status().isOk());
            MainOrder mainOrder = mainOrderService.findById(orderId);
            assertThat(mainOrder.getOrderStatus()).isEqualTo(OrderStatus.forPay);
            assertThat(mainOrder.getOrderItemList()).isNotEmpty();
            //再次下单会提示错误
            mockMvc.perform(post("/order")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(orderBody)))
                    .andExpect(status().is(HttpStatusCustom.SC_DATA_NOT_VALIDATE))
                    .andExpect(jsonPath(RESULT_CODE_PATH).value(ResultCodeEnum.ORDER_NOT_EMPTY.getCode()));*/

    }
}
