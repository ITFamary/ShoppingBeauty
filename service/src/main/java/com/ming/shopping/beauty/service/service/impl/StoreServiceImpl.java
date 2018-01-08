package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.login.Store_;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.repository.StoreRepository;
import com.ming.shopping.beauty.service.service.LoginService;
import com.ming.shopping.beauty.service.service.MerchantService;
import com.ming.shopping.beauty.service.service.StoreService;
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
 * @author helloztt
 */
@Service
public class StoreServiceImpl implements StoreService {
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private LoginService loginService;
    @Autowired
    private MerchantService merchantService;

    @Override
    @Transactional(readOnly = true)
    public Page<Store> findAll(String name, int pageNo, int pageSize) {
        return storeRepository.findAll(
                (root, cq, cb) -> StringUtils.isEmpty(name) ? null : cb.equal(root.get(Store_.name), name)
                , new PageRequest(pageNo, pageSize, new Sort(Sort.Direction.DESC, "id")));
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void addStore(long loginId, long merchantId, String name, String telephone, String contact, Address address) {
        Login login = loginService.findOne(loginId);
        if (login.getStore() != null) {
            throw new ApiResultException(ApiResult.withError(ErrorMessage.ALREADY_MANAGE.getMessage()));
        }
        Merchant merchant = merchantService.findMerchant(merchantId);
        Store store = new Store();
        store.setId(loginId);
        store.setLogin(login);
        store.setName(name);
        store.setTelephone(telephone);
        store.setMerchant(merchant);
        store.setAddress(address);
        store.setManageable(true);
        store.setCreateTime(LocalDateTime.now());
        storeRepository.save(store);
        login.setStore(store);
        login.getLevelSet().add(ManageLevel.storeRoot);
        merchant.getStores().add(store);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void addStore(long loginId, long storeId) {
        Login login = loginService.findOne(loginId);
        if (login.getStore() != null) {
            throw new ApiResultException(ApiResult.withError(ErrorMessage.ALREADY_MANAGE.getMessage()));
        }
        Store store = findStore(storeId);
        Store manage = new Store();
        manage.setId(loginId);
        manage.setLogin(login);
        manage.setStore(store);
        manage.setCreateTime(LocalDateTime.now());
        login.setStore(store);
        login.getLevelSet().add(ManageLevel.storeRoot);
        storeRepository.save(manage);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void freezeOrEnable(long id, boolean enable) {
        Store store = storeRepository.findOne(id);
        if (store == null) {
            throw new ApiResultException(ApiResult.withError(ErrorMessage.STORE_NOT_EXIST.getMessage()));
        }
        store.setEnabled(enable);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void removeStoreManage(long managerId) {
        Store store = storeRepository.findOne(managerId);
        if (store == null) {
            throw new ApiResultException(ApiResult.withError(ErrorMessage.STORE_NOT_EXIST.getMessage()));
        }
        if (store.isManageable()) {
            throw new ApiResultException(ApiResult.withError(ErrorMessage.STORE_CANNOT_DELETE.getMessage()));
        }
        storeRepository.delete(store);
    }

    @Override
    public Store findOne(long id) throws ApiResultException {
        Store store = storeRepository.findOne(id);
        if (store == null) {
            throw new ApiResultException(ApiResult.withError(ErrorMessage.STORE_NOT_EXIST.getMessage()));
        }
        if (!store.isStoreUsable()) {
            throw new ApiResultException(ApiResult.withError(ErrorMessage.STORE_NOT_ENABLE.getMessage()));
        }
        if (!store.isManageable() && !store.isEnabled()) {
            throw new ApiResultException(ApiResult.withError(ErrorMessage.MANAGE_NOT_ENABLE.getMessage()));
        }
        return store;
    }

    @Override
    public Store findStore(long storeId) {
        Store store = storeRepository.findOne((root, cq, cb) ->
                cb.and(cb.equal(root.get(Store_.id), storeId), cb.isTrue(root.get(Store_.manageable)))
        );
        if (store == null) {
            throw new ApiResultException(ApiResult.withError(ErrorMessage.STORE_NOT_EXIST.getMessage()));
        }
        if (!store.isStoreUsable()) {
            throw new ApiResultException(ApiResult.withError(ErrorMessage.STORE_NOT_ENABLE.getMessage()));
        }
        return store;
    }
}
