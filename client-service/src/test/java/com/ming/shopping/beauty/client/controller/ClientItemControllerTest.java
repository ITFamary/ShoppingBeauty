package com.ming.shopping.beauty.client.controller;

import com.ming.shopping.beauty.client.ClientConfigTest;
import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.entity.item.StoreItem_;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.model.definition.ClientStoreItemModel;
import com.ming.shopping.beauty.service.repository.ItemRepository;
import com.ming.shopping.beauty.service.repository.StoreItemRepository;
import com.ming.shopping.beauty.service.service.StagingService;
import me.jiangcai.lib.resource.service.ResourceService;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * @author CJ
 */
public class ClientItemControllerTest extends ClientConfigTest {

    @Autowired
    private StoreItemRepository storeItemRepository;
    @Autowired
    private static ResourceService resourceService;
    @Autowired
    private StagingService stagingService;
    @Autowired
    private ItemRepository itemRepository;
    /**
     * @return 校验器可以校验响应为 api 所规定的Items
     */
    public static ResultMatcher isItemsResponse() {
        return jsonPath("$").value(matchModel(new ClientStoreItemModel(resourceService, false)));
    }


    @Test
    public void go() throws Exception {
        // TODO 添加测试数据


        int size = storeItemRepository.findAll((root, query, cb) ->
                cb.isFalse(root.get(StoreItem_.deleted))
        ).size();
        Login user = mockLogin();
        updateAllRunWith(user);

        Merchant merchant = mockMerchant();
        Store store = mockStore(merchant);
        Item item = mockItem(merchant);
        StoreItem storeItem = mockStoreItem(store, item);
        //符合规范
        mockMvc.perform(get("/items"))
                .andDo(print())
                .andExpect(isItemsResponse())
                .andExpect(jsonPath("$.list", Matchers.hasSize(size+1)));


        //再添加一个
        item.setItemType("豪华洗车");
        itemRepository.save(item);
        mockMvc.perform(get("/items")
                .param("itemType","豪华洗车"))
                .andDo(print())
                .andExpect(isItemsResponse())
                .andExpect(jsonPath("$.list", Matchers.hasSize(1)));

        mockMvc.perform(get("/items")
                .param("storeId",store.getId().toString()))
                .andDo(print())
                .andExpect(isItemsResponse())
                .andExpect(jsonPath("$.list", Matchers.hasSize(1)));
    }



}