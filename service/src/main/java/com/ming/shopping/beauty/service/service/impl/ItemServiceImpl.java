package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.Item_;
import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.entity.item.StoreItem_;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Merchant_;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.login.Store_;
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
    public List<StoreItem> findAllStoreItem(ItemSearcherBody searcher) {
        Specification<StoreItem> specification = (root, cq, cb) -> {
            List<Predicate> conditionList = new ArrayList<>();
            if (searcher.getStoreId() != null && searcher.getStoreId() > 0) {
                conditionList.add(cb.equal(root.join(StoreItem_.store, JoinType.LEFT)
                        .get(Store_.id), searcher.getStoreId()));
            }
            if (searcher.getEnabled() != null) {
                conditionList.add(cb.equal(root.get(StoreItem_.enable), searcher.getEnabled()));
            }
            if (searcher.getRecommended() != null) {
                conditionList.add(cb.equal(root.get(StoreItem_.recommended), searcher.getRecommended()));
            }
            return cb.and(conditionList.toArray(new Predicate[conditionList.size()]));
        };
        return storeItemRepository.findAll(specification);
    }

    @Override
    public Item findById(long id) {
        Item item = itemRepository.findOne(((root, query, cb) ->
                cb.and(cb.equal(root.get(Item_.id), id), cb.isFalse(root.get(Item_.deleted)))));
        if (item == null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.Item_Not_EXIST));
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
    @Transactional(rollbackFor = RuntimeException.class)
    public StoreItem addStoreItem(long storeId, long itemId, BigDecimal salesPrice, boolean recommended) {
        Store store = storeService.findStore(storeId);
        Item item = findById(itemId);
        StoreItem storeItem = new StoreItem();
        storeItem.setStore(store);
        storeItem.setItem(item);
        if (salesPrice != null) {
            //这个价格必须大于等于 项目的销售价
            if (salesPrice.compareTo(item.getSalesPrice()) == -1) {
                throw new ApiResultException(ApiResult.withError(ResultCodeEnum.STORE_ITEM_PRICE_ERROR));
            }
            storeItem.setSalesPrice(salesPrice);
        } else {
            storeItem.setSalesPrice(item.getSalesPrice());
        }
        storeItem.setRecommended(recommended);
        return storeItemRepository.save(storeItem);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void auditItem(long itemId, AuditStatus auditStatus, String comment) {
        Item item = findById(itemId);
        item.setAuditStatus(auditStatus);
        item.setAuditComment(comment);
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
