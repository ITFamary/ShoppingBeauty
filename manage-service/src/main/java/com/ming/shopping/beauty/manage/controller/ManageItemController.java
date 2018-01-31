package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.Item_;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Merchant_;
import com.ming.shopping.beauty.service.entity.support.AuditStatus;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.service.ItemService;
import com.ming.shopping.beauty.service.service.MerchantService;
import com.ming.shopping.beauty.service.utils.Utils;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.supplier.AntDesignPaginationDramatizer;
import me.jiangcai.lib.resource.service.ResourceService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
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
@RequestMapping("/item")
@Controller
@PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "')")
public class ManageItemController extends AbstractCrudController<Item, Long> {

    @Autowired
    private ItemService itemService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private ResourceService resourceService;

    @Override
    @RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
    @PreAuthorize("hasAnyRole('ROOT', '" + Login.ROLE_MERCHANT_ROOT + "')")
    public RowDefinition<Item> list(HttpServletRequest request) {
        return super.list(request);
    }

    /**
     * 添加项目
     *
     * @param item      项目
     * @param otherData 其他信息
     * @return 商户项目列表
     */
    @PostMapping
    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ROOT', '" + Login.ROLE_MERCHANT_ROOT + "')")
    public ResponseEntity addOne(@RequestBody Item item, @RequestBody Map<String, Object> otherData) throws URISyntaxException {
        final String param = "merchantId";

        if (otherData.get(param) == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), param), null));
        }
        if (StringUtils.isEmpty(item.getName())) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "请求数据"), null));
        }
        if (StringUtils.isEmpty((String) otherData.get("imagePath"))) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "请求数据"), null));
        }
        Merchant merchant = merchantService.findOne(Long.parseLong(otherData.get(param).toString()));
        Item responseItem = itemService.addItem(merchant, item, (String) otherData.get("imagePath"));
        return ResponseEntity
                .created(new URI("/item/" + responseItem.getId()))
                .build();
    }

    /**
     * 编辑项目
     * @param item  要编辑的项目信息
     * @param otherData 其他的一些信息
     * @throws URISyntaxException
     */
    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ROOT', '" + Login.ROLE_MERCHANT_ROOT + "')")
    public void updateItem(@RequestBody Item item, @RequestBody Map<String, Object> otherData) throws URISyntaxException{
        addOne(item,otherData);
    }

    /**
     * 项目详情
     * @param id 获取详情的id
     * @return
     */
    @GetMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ROOT', '" + Login.ROLE_MERCHANT_ROOT + "')")
    @Override
    public RowDefinition<Item> getOne(@PathVariable(value = "itemId" ,required = true)Long id){
        return new RowDefinition<Item>(){

            @Override
            public Class<Item> entityClass() {
                return Item.class;
            }

            @Override
            public List<FieldDefinition<Item>> fields() {
                return Arrays.asList(
                        FieldBuilder.asName(Item.class, "id")
                                .build()
                        , FieldBuilder.asName(Item.class, "name")
                                .build()
                        , FieldBuilder.asName(Item.class, "itemType")
                                .build()
                        , FieldBuilder.asName(Item.class, "mainImagePath")
                                .addFormat(Utils.formatResourcePathToURL(resourceService))
                                .build()
                        , FieldBuilder.asName(Item.class, "merchantName")
                                .addSelect(itemRoot -> itemRoot.join(Item_.merchant).get(Merchant_.name))
                                .build()
                        , FieldBuilder.asName(Item.class, "price")
                                .build()
                        , FieldBuilder.asName(Item.class, "salesPrice")
                                .build()
                        , FieldBuilder.asName(Item.class, "auditStatus")
                                .addFormat((data, type) -> {
                                    return data == null ? null : ((AuditStatus) data).getMessage();
                                })
                                .build()
                        , FieldBuilder.asName(Item.class, "enabled")
                                .build()
                        , FieldBuilder.asName(Item.class, "recommended")
                                .build()
                );
            }

            @Override
            public Specification<Item> specification() {
                return (root, query, cb) -> cb.equal(root.get(Item_.id),id);
            }
        };
    }
    /**
     * 项目状态改变/审核
     *
     * @param itemId  项目id
     * @param auditStatus 审核的状态以及备注
     */
    @PutMapping("/{itemId}/auditStatus")
    @PreAuthorize("hasAnyRole('ROOT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setAuditStatus(@PathVariable("itemId") long itemId, @RequestBody Map<String,String> auditStatus) {
        if(auditStatus.get("status") != null && auditStatus.get("comment") != null){
            itemService.auditItem(itemId, AuditStatus.valueOf(auditStatus.get("status")), auditStatus.get("comment"));
        }else{
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), auditStatus), null));
        }
    }

    /**
     * 提交项目审核
     *
     * @param itemId  项目id
     * @param auditStatus    审核的状态以及备注
     */
    @PutMapping("/{itemId}/commit")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ROOT', '" + Login.ROLE_MERCHANT_ROOT + "')")
    public void commitItem(@PathVariable("itemId") long itemId, @RequestBody Map<String,String> auditStatus) {
        if(auditStatus.get("status") != null && auditStatus.get("comment") != null){
            itemService.auditItem(itemId, AuditStatus.valueOf(auditStatus.get("status")), auditStatus.get("comment"));
        }else{
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), auditStatus), null));
        }
    }

    /**
     * 项目批量上架下架
     *
     * @param putData 上下架
     */
    @PutMapping("/enabled")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROOT', '" + Login.ROLE_MERCHANT_ROOT + "')")
    public ApiResult enabled(@RequestBody Map<String, Object> putData) {
        final String param = "enabled";
        final String items = "items";
        //失败的个数
        int count = 0;
        List<Integer> itemList = (List<Integer>) putData.get(items);
        //总个数
        int size = itemList.size();
        if (putData.get(param) != null) {
            if (itemList.size() != 0) {
                for (Integer id : itemList) {
                    try {
                        itemService.freezeOrEnable(Long.parseLong(id.toString()), (boolean) putData.get(param));
                    } catch (Exception e) {
                        count++;
                    }
                }
            } else {
                throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                        , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), items), null));
            }
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), param), null));
        }
        return ApiResult.withOk("总数:" + size + ",成功数:" + (size - count) + ",失败数:" + count);
    }

    /**
     * 项目批量推荐/取消推荐
     *
     * @param putData
     */
    @PutMapping("/recommended")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROOT', '" + Login.ROLE_MERCHANT_ROOT + "')")
    public ApiResult recommended(@RequestBody Map<String, Object> putData) {
        final String param = "recommended";
        final String items = "items";
        //失败的个数
        int count = 0;
        List<Integer> itemList = (List<Integer>) putData.get(items);
        //总个数
        int size = itemList.size();
        if (putData.get(param) != null) {
            if (itemList.size() != 0) {
                for (Integer id : itemList) {
                    itemService.recommended(Long.parseLong(id.toString()), (boolean) putData.get(param));
                }
            } else {
                throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                        , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), items), null));
            }
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), param), null));
        }
        return ApiResult.withOk("总数:" + size + ",成功数:" + (size - count) + ",失败数:" + count);
    }


    @Override
    protected List<FieldDefinition<Item>> listFields() {
        return Arrays.asList(
                FieldBuilder.asName(Item.class, "id")
                        .build()
                , FieldBuilder.asName(Item.class, "name")
                        .build()
                , FieldBuilder.asName(Item.class, "itemType")
                        .build()
                , FieldBuilder.asName(Item.class, "mainImagePath")
                        .addFormat(Utils.formatResourcePathToURL(resourceService))
                        .build()
                , FieldBuilder.asName(Item.class, "merchantName")
                        .addSelect(itemRoot -> itemRoot.join(Item_.merchant).get(Merchant_.name))
                        .build()
                , FieldBuilder.asName(Item.class, "price")
                        .build()
                , FieldBuilder.asName(Item.class, "salesPrice")
                        .build()
                , FieldBuilder.asName(Item.class, "auditStatus")
                        .addFormat((data, type) -> {
                            return data == null ? null : ((AuditStatus) data).getMessage();
                        })
                        .build()
                , FieldBuilder.asName(Item.class, "enabled")
                        .build()
                , FieldBuilder.asName(Item.class, "recommended")
                        .build()
        );
    }

    @Override
    protected Specification<Item> listSpecification(Map<String, Object> queryData) {
        return ((root, query, cb) -> {
            List<Predicate> conditions = new ArrayList<>();
            if (queryData.get("itemName") != null) {
                conditions.add(cb.like(root.get(Item_.name), "%" + queryData.get("itemName") + "%"));
            }
            if (queryData.get("itemType") != null) {
                conditions.add(cb.equal(root.get(Item_.itemType), queryData.get("itemType")));
            }
            if (queryData.get("merchantName") != null) {
                conditions.add(cb.equal(root.join(Item_.merchant, JoinType.LEFT)
                        .get(Merchant_.name), queryData.get("merchantName")));
            }
            if (queryData.get("merchantId") != null) {
                conditions.add(cb.equal(root.join(Item_.merchant).get(Merchant_.id), queryData.get("merchantId")));
            }
            if (queryData.get("auditStatus") != null){
                conditions.add(cb.equal(root.get(Item_.auditStatus)
                        ,AuditStatus.valueOf(queryData.get("auditStatus").toString())));
            }
            if (queryData.get("enabled") != null) {
                if ((boolean) queryData.get("enabled"))
                    conditions.add(cb.isTrue(root.get(Item_.enabled)));
                else
                    conditions.add(cb.isFalse(root.get(Item_.enabled)));
            }
            if (queryData.get("recommended") != null) {
                if ((boolean) queryData.get("recommended"))
                    conditions.add(cb.isTrue(root.get(Item_.recommended)));
                else
                    conditions.add(cb.isFalse(root.get(Item_.recommended)));
            }

            conditions.add(cb.isFalse(root.get(Item_.deleted)));
            return cb.and(conditions.toArray(new Predicate[conditions.size()]));
        });
    }

    @Override
    protected List<Order> listOrder(CriteriaBuilder criteriaBuilder, Root<Item> root) {
        return Arrays.asList(
                criteriaBuilder.desc(root.get(Item_.id))
        );
    }

    @Override
    @PreAuthorize("denyAll()")
    public void deleteOne(Long aLong) {
        super.deleteOne(aLong);
    }

    @Override
    @PreAuthorize("denyAll()")
    public RowDefinition<Item> getDetail(Long aLong) {
        return null;
    }
}
