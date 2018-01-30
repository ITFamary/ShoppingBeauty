package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.ManageConfigTest;
import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Store;
import org.junit.Test;

/**
 * @author
 */
public class ManageStroeItemControllerTest extends ManageConfigTest{

    @Test
    public void listTest() throws Exception {

        Merchant merchant = mockMerchant();
        updateAllRunWith(merchant.getLogin());
        Store store = mockStore(merchant);
        Item item = mockItem(merchant);
    }
}
