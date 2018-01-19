package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Merchant_;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.repository.MerchantRepository;
import com.ming.shopping.beauty.service.service.LoginService;
import com.ming.shopping.beauty.service.service.MerchantService;
import me.jiangcai.jpa.entity.support.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lxf
 */
@Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private LoginService loginService;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Merchant addMerchant(long loginId, String name, String telephone, String contact, Address address) throws ApiResultException {
        Login login = loginService.findOne(loginId);
        if (login.getMerchant() != null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.LOGIN_MERCHANT_EXIST));
        }
        Merchant merchant = new Merchant();
        login.setMerchant(merchant);
        login.addLevel(ManageLevel.merchantRoot);
        merchant.setId(login.getId());
        merchant.setLogin(login);
        merchant.setName(name);
        merchant.setTelephone(telephone);
        merchant.setContact(contact);
        merchant.setAddress(address);
        merchant.setManageable(true);
        merchant.setCreateTime(LocalDateTime.now());
        return merchantRepository.save(merchant);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Merchant addMerchant(long loginId, Merchant merchant) throws ApiResultException {
        Login login = loginService.findOne(loginId);
        if (login.getMerchant() != null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.LOGIN_MERCHANT_EXIST));
        }
        login.setMerchant(merchant);
        login.addLevel(ManageLevel.merchantRoot);
        merchant.setId(login.getId());
        merchant.setLogin(login);
        merchant.setManageable(true);
        merchant.setCreateTime(LocalDateTime.now());
        return merchantRepository.save(merchant);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Merchant addMerchant(long loginId, long merchantId) throws ApiResultException {
        Login login = loginService.findOne(loginId);
        if (login.getMerchant() != null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.LOGIN_MERCHANT_EXIST));
        }
        Merchant merchant = findMerchant(merchantId);
        Merchant manage = new Merchant();
        login.setMerchant(manage);
        login.addLevel(ManageLevel.merchantManager);
        manage.setId(login.getId());
        manage.setLogin(login);
        manage.setMerchant(merchant);
        manage.setCreateTime(LocalDateTime.now());
        return merchantRepository.save(manage);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void freezeOrEnable(long id, boolean enable) throws ApiResultException {
        Merchant merchant = merchantRepository.findOne(id);
        if (merchant == null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.MERCHANT_NOT_EXIST));
        }
        merchant.setEnabled(enable);
        merchantRepository.save(merchant);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void removeMerchantManage(long managerId) throws ApiResultException {
        Merchant merchant = merchantRepository.findOne(managerId);
        if (merchant.isManageable()) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.MERCHANT_CANNOT_DELETE));
        }
        merchant.getLogin().setMerchant(null);
        merchantRepository.delete(merchant);
    }

    @Override
    public Merchant findOne(long merchantId) throws ApiResultException {
        Merchant merchant = merchantRepository.findOne(merchantId);
        if (merchant == null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.LOGIN_NOT_EXIST));
        }
        if (merchant.isMerchantUsable()) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.MERCHANT_NOT_ENABLE));
        }
        if (!merchant.isManageable() && !merchant.isEnabled()) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.MANAGE_NOT_ENABLE));
        }
        return merchant;
    }

    @Override
    public Merchant findMerchant(long merchantId) throws ApiResultException {
        Merchant merchant = merchantRepository.findOne((root, cq, cb) ->
                cb.and(cb.equal(root.get(Merchant_.id), merchantId), cb.isTrue(root.get(Merchant_.manageable))));
        if (merchant == null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.MERCHANT_NOT_EXIST));
        }
        if (!merchant.isMerchantUsable()) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.MERCHANT_NOT_ENABLE));
        }
        return merchant;
    }
}
