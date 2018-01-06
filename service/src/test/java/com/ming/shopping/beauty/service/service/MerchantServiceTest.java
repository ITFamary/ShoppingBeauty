package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.CoreServiceTest;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * Created by helloztt on 2018/1/7.
 */
public class MerchantServiceTest extends CoreServiceTest {

    private Merchant mockMerchant;

    @Before
    public void init() {
        mockMerchant = mockMerchant();
    }

    @Test
    public void addMerchant() throws Exception {
        assertThat(mockMerchant).isNotNull();
        assertThat(mockMerchant.isManageable()).isTrue();
        //看看关联有没有问题
        assertThat(mockMerchant.getLogin()).isNotNull();
        assertThat(mockMerchant.getLogin().getMerchant()).isNotNull();
    }

    @Test
    public void addMerchantManage() throws Exception {
        Merchant mockManage = mockMerchant(mockMerchant.getId());
        assertThat(mockManage).isNotNull();
        assertThat(mockManage.isManageable()).isFalse();
        assertThat(mockManage.getMerchant()).isEqualTo(mockMerchant);
    }

    @Test
    public void freezeOrEnable() throws Exception {
        assertThat(mockMerchant.isEnabled()).isTrue();
        merchantService.freezeOrEnable(mockMerchant.getId(), !mockMerchant.isEnabled());
        try {
            merchantService.findMerchant(mockMerchant.getId());
            throw new Exception();
        } catch (ApiResultException ex) {
            assertThat(ex.getApiResult().getMessage()).isEqualTo("商户已冻结");
        }
        merchantService.freezeOrEnable(mockMerchant.getId(), mockMerchant.isEnabled());
    }

    @Test
    public void removeMerchantManage() throws Exception {
        Merchant removeManage = mockMerchant(mockMerchant.getId());
        merchantService.removeMerchantManage(removeManage.getId());
        try{
            merchantService.findMerchant(removeManage.getId());
        }catch (ApiResultException ex){
            assertThat(ex.getApiResult().getMessage()).isEqualTo("管理员不存在");
        }
        Login login = loginService.findOne(removeManage.getId());
        assertThat(login.getMerchant()).isNull();
    }

    @Test
    public void findAll() throws Exception {
    }

}