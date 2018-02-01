package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.ManageConfigTest;
import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.model.request.NewStoreItemBody;
import me.jiangcai.lib.test.matcher.NumberMatcher;
import org.hamcrest.core.IsEqual;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author
 */
public class ManageStoreItemControllerTest extends ManageConfigTest{

    @Test
    public void listTest() throws Exception {
        //创建一个商户,以他来运行
        Merchant merchant = mockMerchant();
        updateAllRunWith(merchant.getLogin());
        //创建一个门店
        Store store = mockStore(merchant);
        //创建一个项目
        Item item = mockItem(merchant);

        //以门店运行吧
        updateAllRunWith(store.getLogin());
        //添加到门店项目
        NewStoreItemBody nsi = new NewStoreItemBody();
        nsi.setItemId(item.getId());
        nsi.setStoreId(store.getId());
        nsi.setSalesPrice(item.getSalesPrice().setScale(2).subtract(BigDecimal.valueOf(0.10)));
        //价格设置不对,添加失败
        mockMvc.perform(post("/storeItem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nsi)))
                .andDo(print())
                .andExpect(status().is(210));

        //设置正确的
        nsi.setSalesPrice(item.getSalesPrice().setScale(2).add(BigDecimal.valueOf(0.20)));
        mockMvc.perform(post("/storeItem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nsi)))
                .andDo(print())
                .andExpect(status().isCreated());

        //再添加一个
        NewStoreItemBody nsi1 = new NewStoreItemBody();
        nsi1.setItemId(item.getId());
        nsi1.setStoreId(store.getId());
        nsi1.setSalesPrice(item.getSalesPrice().setScale(2).add(BigDecimal.valueOf(0.10)));
        mockMvc.perform(post("/storeItem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nsi1)))
                .andExpect(status().isCreated());

        System.out.println(nsi.getSalesPrice()+"-------------------------------------------------------------"+nsi1.getSalesPrice());

        //查看列表
        mockMvc.perform(get("/storeItem"))
                .andDo(print())
                .andExpect(jsonPath("$.list[0].salesPrice").value(NumberMatcher.numberAsDoubleEquals(nsi1.getSalesPrice())))
                .andExpect(jsonPath("$.list[1].salesPrice").value(NumberMatcher.numberAsDoubleEquals(nsi.getSalesPrice())));
    }
}
