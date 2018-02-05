package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.service.entity.item.Item_;
import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.entity.item.StoreItem_;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Store_;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.service.StoreItemService;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.supplier.AntDesignPaginationDramatizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author lxf
 */
@Controller
@RequestMapping("/storeItem")
@PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_READ + "','" + Login.ROLE_PLATFORM_READ + "','" + Login.ROLE_STORE_ROOT + "')")
public class ManageStoreItemController extends AbstractCrudController<StoreItem, Long> {

    @Autowired
    private StoreItemService storeItemService;

    @Override
    @ResponseStatus(HttpStatus.OK)
    @RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
    public RowDefinition<StoreItem> list(HttpServletRequest request) {
        return super.list(request);
    }

    /**
     * 添加门店项目
     *
     * @param postData  门店项目
     * @param otherData 其他信息
     * @return
     * @throws URISyntaxException
     */
    @PostMapping
    @Override
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_STORE + "','" + Login.ROLE_MERCHANT_ITEM + "','" + Login.ROLE_STORE_ROOT + "')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity addOne(@RequestBody StoreItem postData, @RequestBody Map<String, Object> otherData) throws URISyntaxException {
        final String storeId = "storeId";
        final String itemId = "itemId";
        if (otherData.get(storeId) == null || otherData.get(itemId) == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), storeId + itemId), null));
        }
        StoreItem storeItem = storeItemService.addStoreItem(Long.parseLong(otherData.get(storeId).toString()), Long.parseLong(otherData.get(itemId).toString()), postData);
        return ResponseEntity
                .created(new URI("/storeItem/" + storeItem.getId()))
                .build();
    }

    /**
     * 修改门店项目的 销售价/会员价
     *
     * @param salesPrice 新价格
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_STORE + "','" + Login.ROLE_MERCHANT_ITEM + "','" + Login.ROLE_STORE_ROOT + "')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateStoreItem(@PathVariable(value = "id", required = true) long id, @RequestBody BigDecimal salesPrice) {
        storeItemService.updateStoreItem(id, salesPrice);
    }

    /**
     * 批量推荐/取消推荐/门店项目(可以根据itemId批量操作)
     *
     * @param putData 操作的信息
     * @return 成功失败的数量
     */
    @PutMapping("/storeItemUpdater/recommended")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_STORE + "','" + Login.ROLE_MERCHANT_ITEM + "','" + Login.ROLE_STORE_ROOT + "')")
    @ResponseBody
    public ApiResult batchRecommended(@RequestBody Map<String, Object> putData) {
        final String recommended = "recommended";
        final String storeItems = "storeItems";
        //失败的个数
        int count = 0;
        List<Integer> itemList = (List<Integer>) putData.get(storeItems);
        //总个数
        int size = itemList.size();
        if (putData.get(recommended) != null || putData.get(storeItems) != null) {
            for (Integer storeItemId : itemList) {
                try {
                    storeItemService.recommended((boolean) putData.get(recommended), Long.parseLong(storeItemId.toString()));
                } catch (Exception e) {
                    count++;
                }
            }
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "缺少必要数据"), null));
        }
        return ApiResult.withOk("总数:" + size + ",成功数:" + (size - count) + ",失败数:" + count);
    }

    /**
     * 批量上架/下架门店项目(可以根据itemId批量操作)
     *
     * @param putData 操作的信息
     * @return 成功失败的数量
     */
    @PutMapping("/storeItemUpdater/enabled")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_STORE + "','" + Login.ROLE_MERCHANT_ITEM + "','" + Login.ROLE_STORE_ROOT + "')")
    @ResponseBody
    public ApiResult batchEnable(@RequestBody Map<String, Object> putData) {
        final String enabled = "enabled";
        final String storeItems = "storeItems";
        //失败的个数
        int count = 0;
        List<Integer> itemList = (List<Integer>) putData.get(storeItems);
        //总个数
        int size = itemList.size();
        if (putData.get(enabled) != null || putData.get(storeItems) != null) {
            for (Integer storeItemId : itemList) {
                try {
                    storeItemService.freezeOrEnable((boolean) putData.get(enabled), Long.parseLong(storeItemId.toString()));
                } catch (Exception e) {
                    count++;
                }
            }

        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "缺少必要数据"), null));
        }
        return ApiResult.withOk("总数:" + size + ",成功数:" + (size - count) + ",失败数:" + count);
    }

    @Override
    protected List<FieldDefinition<StoreItem>> listFields() {
        return Arrays.asList(
                FieldBuilder.asName(StoreItem.class, "id")
                        .build()
                , FieldBuilder.asName(StoreItem.class, "name")
                        .addSelect(storeItemRoot -> storeItemRoot.join(StoreItem_.item).get(Item_.name))
                        .build()
                , FieldBuilder.asName(StoreItem.class, "storeName")
                        .addSelect(storeItemRoot -> storeItemRoot.join(StoreItem_.store).get(Store_.name))
                        .build()
                , FieldBuilder.asName(StoreItem.class, "price")
                        .addSelect(storeItemRoot -> storeItemRoot.join(StoreItem_.item).get(Item_.price))
                        .build()
                , FieldBuilder.asName(StoreItem.class, "salesPrice")
                        .build()
                , FieldBuilder.asName(StoreItem.class, "enabled")
                        .addSelect(storeItemRoot -> storeItemRoot.get(StoreItem_.enable))
                        .build()
                , FieldBuilder.asName(StoreItem.class, "recommended")
                        .build()
        );
    }

    @Override
    protected Specification<StoreItem> listSpecification(Map<String, Object> queryData) {
        return ((root, query, cb) -> {
            List<Predicate> conditionList = new ArrayList<>();
            if (queryData.get("storeId") != null)
                conditionList.add(cb.equal(root.join(StoreItem_.store).get(Store_.id), queryData.get("storeId")));
            if (queryData.get("itemName") != null)
                conditionList.add(cb.like(root.join(StoreItem_.item).get(Item_.name), "%" + queryData.get("itemName") + "%"));
            if (queryData.get("storeName") != null)
                conditionList.add(cb.like(root.join(StoreItem_.store).get(Store_.name), "%" + queryData.get("storeName") + "%"));
            if (queryData.get("enabled") != null) {
                if ((boolean) queryData.get("enabled"))
                    conditionList.add(cb.isTrue(root.get(StoreItem_.enable)));
                else
                    conditionList.add(cb.isFalse(root.get(StoreItem_.enable)));
            }
            if (queryData.get("recommended") != null) {
                if ((boolean) queryData.get("recommended"))
                    conditionList.add(cb.isTrue(root.get(StoreItem_.recommended)));
                else
                    conditionList.add(cb.isFalse(root.get(StoreItem_.recommended)));
            }
            conditionList.add(cb.isFalse(root.get(StoreItem_.deleted)));
            return cb.and(conditionList.toArray(new Predicate[conditionList.size()]));
        });
    }

    @Override
    protected List<Order> listOrder(CriteriaBuilder criteriaBuilder, Root<StoreItem> root) {
        return Arrays.asList(criteriaBuilder.desc(root.get(StoreItem_.id)));
    }
}
