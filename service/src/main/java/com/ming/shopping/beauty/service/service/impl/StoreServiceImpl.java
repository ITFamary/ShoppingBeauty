package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.Represent;
import com.ming.shopping.beauty.service.entity.Store;
import com.ming.shopping.beauty.service.entity.User;
import com.ming.shopping.beauty.service.repository.RepresentRepository;
import com.ming.shopping.beauty.service.repository.StoreRepository;
import com.ming.shopping.beauty.service.service.StoreService;
import me.jiangcai.jpa.entity.support.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StoreServiceImpl implements StoreService {
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private RepresentRepository representRepository;

    @Override
    @Transactional(readOnly = true)
    public List getAllStores() {
        return storeRepository.findAll();
    }

    @Override
    @Transactional
    public void addStore(String storeName, Address address, String telephone) {
        Store store = new Store();
        store.setStoreName(storeName);
        store.setAddress(address);
        store.setTelephone(telephone);
        store.setCreateTime(LocalDateTime.now());
        storeRepository.save(store);
    }

    @Override
    @Transactional
    public void freezeOrEnable(long id ,boolean enable) {
        Store store = storeRepository.getOne(id);
        store.setEnable(enable);
    }

    @Override
    @Transactional
    public void addRepresent(long id ,Represent represent) {
        Store store = storeRepository.getOne(id);
        List<Represent> represents = store.getRepresents();
        if(represents == null){
            represents = new ArrayList<>();
        }
        represents.add(represent);
        store.setRepresents(represents);
    }

    @Override
    public void freezeOrEnableRepresent(long id ,Represent represent, boolean enable) {
        Store store = storeRepository.getOne(id);
        List<Represent> represents = store.getRepresents();
        if(represents.contains(represent)){
            Represent represent1 = representRepository.getOne(represent.getId());
            represent1.setEnable(enable);
        };
    }
}
