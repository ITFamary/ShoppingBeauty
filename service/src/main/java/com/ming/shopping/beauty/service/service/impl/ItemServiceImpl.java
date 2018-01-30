package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.Item_;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Merchant_;
import com.ming.shopping.beauty.service.entity.support.AuditStatus;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.request.ItemSearcherBody;
import com.ming.shopping.beauty.service.repository.ItemRepository;
import com.ming.shopping.beauty.service.repository.StoreItemRepository;
import com.ming.shopping.beauty.service.service.ItemService;
import com.ming.shopping.beauty.service.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private StoreService storeService;
    @Autowired
    private StoreItemRepository storeItemRepository;

    @Override
    public List<Item> findAll(ItemSearcherBody searcher) {
        Specification<Item> specification = (root, cq, cb) -> {
            List<Predicate> conditionList = new ArrayList<>();
            if (searcher.getMerchantId() != null && searcher.getMerchantId() > 0) {
                conditionList.add(cb.equal(root.join(Item_.merchant, JoinType.LEFT)
                        .get(Merchant_.id), searcher.getMerchantId()));
            }
            if (searcher.getEnabled() != null) {
                conditionList.add(cb.equal(root.get(Item_.enable), searcher.getEnabled()));
            }
            if (searcher.getRecommended() != null) {
                conditionList.add(cb.equal(root.get(Item_.recommended), searcher.getRecommended()));
            }
            return cb.and(conditionList.toArray(new Predicate[conditionList.size()]));
        };
        return itemRepository.findAll(specification);
    }

    @Override
    public Item findOne(long id) {
        Item item = itemRepository.findOne(((root, query, cb) ->
                cb.and(cb.equal(root.get(Item_.id), id), cb.isFalse(root.get(Item_.deleted)))));
        if (item == null || item.isDeleted()) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.ITEM_NOT_EXIST));
        }
        return item;
    }

    @Override
    @Transactional
    public Item addItem(Merchant merchant, String thumbnailUrl, String name, String itemType, BigDecimal price, BigDecimal salesPrice,
                        BigDecimal costPrice, String description, String richDescription, boolean recommended) {
        Item item = new Item();
        if (merchant != null) {
            item.setMerchant(merchant);
        }
        item.setName(name);
        item.setThumbnailUrl(thumbnailUrl);
        item.setItemType(itemType);
        item.setPrice(price);
        item.setSalesPrice(salesPrice);
        item.setCostPrice(costPrice);
        item.setDescription(description);
        item.setAuditStatus(AuditStatus.NOT_SUBMIT);
        if (richDescription != null) {
            item.setRichDescription(richDescription);
        }
        item.setRecommended(recommended);
        return itemRepository.save(item);
    }

    @Override
    @Transactional
    public Item addItem(Merchant merchant, Item item) {
            if (item.getId() != null) {
                //编辑
                Item findOld = findOne(item.getId());
                if (!findOld.isEnable()) {
                    //下架才可以编辑
                    findOld.setName(item.getName());
                    findOld.setThumbnailUrl(item.getThumbnailUrl());
                    findOld.setPrice(item.getPrice());
                    findOld.setSalesPrice(item.getSalesPrice());
                    findOld.setCostPrice(item.getCostPrice());
                    findOld.setDescription(item.getDescription());
                    findOld.setRichDescription(item.getRichDescription());
                    item.setAuditStatus(AuditStatus.NOT_SUBMIT);
                } else {
                    throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                            , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "请求数据"), null));
                }
            } else {
                //新增
                if (merchant != null) {
                    item.setMerchant(merchant);
                }
                item.setAuditStatus(AuditStatus.NOT_SUBMIT);
            }
        return itemRepository.save(item);
    }


    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void auditItem(long itemId, AuditStatus auditStatus, String comment) {
        Item item = findOne(itemId);
        item.setAuditStatus(auditStatus);
        item.setAuditComment(comment);
    }

    @Override
    @Transactional
    public Item freezeOrEnable(long id, boolean enable) {
        Item item = findOne(id);
        if (item.getAuditStatus() == AuditStatus.AUDIT_PASS)
            item.setEnable(enable);
        else
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.ITEM_NOT_AUDIT));
        return itemRepository.save(item);
    }

    @Override
    @Transactional
    public Item showOrHidden(long id, boolean deleted) {
        Item item = findOne(id);
        item.setDeleted(deleted);
        return itemRepository.save(item);
    }

    @Override
    @Transactional
    public Item setMerchant(long id, Merchant merchant) {
        Item item = findOne(id);
        item.setMerchant(merchant);
        return itemRepository.save(item);
    }

    @Override

    @Transactional
    public Item recommended(long id, boolean recommended) {
        Item one = findOne(id);
        one.setRecommended(recommended);
        return itemRepository.save(one);
    }


}
