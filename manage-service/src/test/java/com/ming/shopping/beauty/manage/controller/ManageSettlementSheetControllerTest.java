package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.ManageConfigTest;
import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Represent;
import com.ming.shopping.beauty.service.entity.login.Store;
import org.junit.Test;
import org.springframework.mock.web.MockHttpSession;

/**
 * @author lxf
 */
public class ManageSettlementSheetControllerTest extends ManageConfigTest {

    @Test
    public void go()throws Exception{
        //商户
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

    }
}
