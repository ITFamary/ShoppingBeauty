package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.ManageConfigTest;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.model.request.NewStoreBody;
import com.ming.shopping.beauty.service.repository.StoreRepository;
import com.ming.shopping.beauty.service.service.StoreService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author lxf
 */
public class ManageStoreControllerTest extends ManageConfigTest {

    @Autowired
    private StoreService storeService;
    @Autowired
    private StoreRepository storeRepository;

    @Test
    public void storeList() throws Exception {
        //首先是不具有管理员权限的人访问,拒绝访问
        //随便来个人只要不是管理员
        Login fackManage = mockLogin();
        //身份运行
        updateAllRunWith(fackManage);
        mockMvc.perform(get("/store"))
                .andExpect(status().isForbidden());
        //来个商户
        Merchant merchant = mockMerchant();
        //用这个商户来运行
        updateAllRunWith(merchant.getLogin());
        //将要成为门店的用户
        Login willStore = mockLogin();


        //添加一个门店
        NewStoreBody rsb = new NewStoreBody();
        rsb.setContact("王女士");
        rsb.setName("测试的门店");
        rsb.setTelephone("18799882273");
        rsb.setLoginId(willStore.getId());
        rsb.setMerchantId(merchant.getId());
        System.out.println(objectMapper.writeValueAsString(rsb));
        //发送请求添加
        String location = mockMvc.perform(post("/store")
                .content(objectMapper.writeValueAsString(rsb))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        Store store = storeService.findStore(willStore.getId());
        assertThat(store != null).isTrue();
        //再添加一个
        Store store1 = mockStore(merchant);
        //获取门店列表
        String telephone = "{telephone:" + merchant.getTelephone() + "}";
        String contentAsString = mockMvc.perform(get("/store")
                .contentType(MediaType.APPLICATION_JSON)
                .content(telephone))
                .andDo(print())
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String s = objectMapper.readTree(contentAsString).get("list").get(0).get("id").asText();
        List<Long> oldIdList = new ArrayList<>();
        oldIdList.add(store.getId());
        oldIdList.add(store1.getId());
        assertThat(oldIdList.contains(Long.parseLong(s))).isTrue();

        boolean enable = false;
        //禁用门店
        mockMvc.perform(put("/store/" + store.getId()+"/enabled")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enable)))
                .andDo(print())
                .andExpect(status().isNoContent());

        Store findOne = storeRepository.getOne(store.getId());
        assertThat(findOne.isEnabled()).isFalse();

    }
}
