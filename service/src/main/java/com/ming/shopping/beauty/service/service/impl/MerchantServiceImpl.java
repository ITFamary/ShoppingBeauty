package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.repository.MerchantRepository;
import com.ming.shopping.beauty.service.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author lxf
 */
@Service
public class MerchantServiceImpl implements MerchantService{

    @Autowired
    private MerchantRepository merchantRepository;

    @Override
    @Transactional(readOnly = true)
    public List getAllMerchant() {
        return merchantRepository.findAll();
    }

    @Override
    @Transactional
    public void addMerchant(Login login, String merchantName, String password) {
//        Merchant merchant = new Merchant();
//        merchant.setLoginName(merchantName);
//        merchant.setPassword(password);
//        merchant.setCreateTime(LocalDateTime.now());
//        merchantRepository.save(merchant);
    }

    @Override
    @Transactional
    public void resetPassword(long id,String password) {
//        Merchant merchant = merchantRepository.getOne(id);
//        merchant.setPassword(password);
    }

    @Override
    @Transactional
    public void freezeOrEnable(long id,boolean enable) {
//        Merchant merchant = merchantRepository.getOne(id);
//        merchant.getLogin(enable);
    }
}
