package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Merchant_;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
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

import java.time.LocalDateTime;

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
    @Transactional(readOnly = true)
    public Page<Merchant> findAll(String name, int pageNo, int pageSize) {
        return merchantRepository.findAll(
                (root, cq, cb) ->
                        StringUtils.isEmpty(name) ? null : cb.like(root.get(Merchant_.name), "%" + name + "%")
                , new PageRequest(pageNo, pageSize, new Sort(Sort.Direction.DESC, "id")));
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Merchant addMerchant(long loginId, String name, String telephone, String contact, Address address) throws ApiResultException {
        Login login = loginService.findOne(loginId);
        if (login.getMerchant() != null) {
            throw new ApiResultException(ApiResult.withError("该用户已是商户"));
        }
        Merchant merchant = new Merchant();
        login.setMerchant(merchant);
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
    public Merchant addMerchant(long loginId, long merchantId) throws ApiResultException {
        Login login = loginService.findOne(loginId);
        if (login == null) {
            throw new ApiResultException(ApiResult.withError("用户不存在"));
        }
        if (login.getMerchant() != null) {
            throw new ApiResultException(ApiResult.withError("该用户已是商户"));
        }
        Merchant merchant = findMerchant(merchantId);
        Merchant manage = new Merchant();
        login.setMerchant(manage);
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
            throw new ApiResultException(ApiResult.withError("商户不存在"));
        }
        merchant.setEnabled(enable);
        merchantRepository.save(merchant);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void removeMerchantManage(long managerId) throws ApiResultException {
        Merchant merchant = merchantRepository.findOne(managerId);
        if (merchant.isManageable()) {
            throw new ApiResultException(ApiResult.withError("商户不可删除"));
        }
        merchant.getLogin().setMerchant(null);
        merchantRepository.delete(merchant);
    }

    @Override
    public Merchant findOne(long merchantId) throws ApiResultException {
        Merchant merchant = merchantRepository.findOne(merchantId);
        if (merchant == null) {
            throw new ApiResultException(ApiResult.withError("管理员不存在"));
        }
        if (merchant.isMerchantUsable()) {
            throw new ApiResultException(ApiResult.withError("商户已冻结"));
        }
        if (!merchant.isManageable() && !merchant.isEnabled()) {
            throw new ApiResultException(ApiResult.withError("管理员已冻结"));
        }
        return merchant;
    }

    @Override
    public Merchant findMerchant(long merchantId) throws ApiResultException {
        Merchant merchant = merchantRepository.findOne((root, cq, cb) ->
                cb.and(cb.equal(root.get("id"), merchantId), cb.isTrue(root.get(Merchant_.manageable))));
        if (merchant == null) {
            throw new ApiResultException(ApiResult.withError("管理员不存在"));
        }
        if (!merchant.isMerchantUsable()) {
            throw new ApiResultException(ApiResult.withError("商户已冻结"));
        }
        return merchant;
    }
}
