package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.CoreServiceTest;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.utils.Constant;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Page;

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
            assertThat(ex.getApiResult().getMessage()).isEqualTo(MerchantService.ErrorMessage.MERCHANT_NOT_ENABLE.getMessage());
        }
        merchantService.freezeOrEnable(mockMerchant.getId(), mockMerchant.isEnabled());
    }

    @Test
    public void removeMerchantManage() throws Exception {
        Merchant removeManage = mockMerchant(mockMerchant.getId());
        merchantService.removeMerchantManage(removeManage.getId());
        try {
            merchantService.findOne(removeManage.getId());
        } catch (ApiResultException ex) {
            assertThat(ex.getApiResult().getMessage()).isEqualTo(MerchantService.ErrorMessage.MERCHANT_OR_MANAGE_NOT_EXIST.getMessage());
        }
        Login login = loginService.findOne(removeManage.getId());
        assertThat(login.getMerchant()).isNull();
    }

    @Test
    public void findAll() throws Exception {
        Page<Merchant> allMerchant = merchantService.findAll(null, null, 0, Constant.MANAGE_PAGE_SIZE);
        Page<Merchant> merchantPage = merchantService.findAll(null, true, 0, Constant.MANAGE_PAGE_SIZE);
        Page<Merchant> managePage = merchantService.findAll(null, false, 0, Constant.MANAGE_PAGE_SIZE);
        assertThat(allMerchant);
        assertThat(merchantPage);
        assertThat(managePage);
        assertThat(allMerchant.getTotalElements())
                .isGreaterThan(0L)
                .isEqualTo(merchantPage.getTotalElements() + managePage.getTotalElements());

    }

}