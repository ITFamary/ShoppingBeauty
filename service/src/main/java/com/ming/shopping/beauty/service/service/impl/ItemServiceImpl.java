package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.repository.ItemRepository;
import com.ming.shopping.beauty.service.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Override
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    @Override
    public Item findById(long id) {
        return itemRepository.findOne(id);
    }

    @Override
    @Transactional
    public Item addItem(String code, Merchant merchant, String name, String itemType, BigDecimal price, BigDecimal salesPrice,
                        BigDecimal costPrice, String description, String richDescription, boolean recommended) {
        Item item = new Item();
        item.setCode(code);
        if (merchant != null) {
            item.setMerchant(merchant);
        }
        item.setName(name);
        item.setItemType(itemType);
        item.setPrice(price);
        item.setSalesPrice(salesPrice);
        item.setCostPrice(costPrice);
        item.setDescription(description);
        if (richDescription != null) {
            item.setRichDescription(richDescription);
        }
        item.setRecommended(recommended);
        return itemRepository.save(item);
    }

    @Override
    @Transactional
    public Item freezeOrEnable(long id, boolean enable) {
        Item item = findById(id);
        item.setEnable(enable);
        return itemRepository.save(item);
    }

    @Override
    @Transactional
    public Item showOrHidden(long id, boolean deleted) {
        Item item = findById(id);
        item.setDeleted(deleted);
        return itemRepository.save(item);
    }

    @Override
    @Transactional
    public Item setMerchant(long id, Merchant merchant) {
        Item item = findById(id);
        item.setMerchant(merchant);
        return itemRepository.save(item);
    }


}
