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
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
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
@PreAuthorize("hassAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "')")
public class ManageItemController extends AbstractCrudController<Item, Long> {

    @Autowired
    private ItemService itemService;
    @Autowired
    private MerchantService merchantService;


    @Override
    @PreAuthorize("hasAnyRole('ROOT', '" + Login.ROLE_MERCHANT_ROOT + "')")
    @GetMapping("/{itemId}")
    public Object getOne(Long aLong) {
        return super.getOne(aLong);
    }

    /**
     * 添加项目/编辑项目
     *
     * @param item      项目
     * @param otherData 其他信息
     * @return 商户项目列表
     */
    @PostMapping
    @Override
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity addOne(@RequestBody Item item,@RequestBody Map<String, Object> otherData) throws URISyntaxException {
        final String param = "merchantId";

        if (otherData.get(param) == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), param), null));
        }
        if (StringUtils.isEmpty(item.getName())) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "请求数据"), null));
        }
        Merchant merchant = merchantService.findOne((long) otherData.get(param));
        Item responseItem = itemService.addItem(merchant, item);
        return ResponseEntity
                .created(new URI("/item/" + responseItem.getId()))
                .build();
    }

    /**
     * 项目状态改变/审核
     *
     * @param itemId 项目id
     * @param status 状态
     * @param message 审核结果备注
     */
    @PutMapping("/{itemId}/auditStatus")
    @PreAuthorize("hassAnyRole('ROOT')")
    @ResponseStatus(HttpStatus.OK)
    public void setAuditStatus(@PathVariable("itemId") long itemId, @RequestBody AuditStatus status, @RequestHeader("comment") String message) {
        itemService.auditItem(itemId, status, message);
    }

    /**
     * 提交项目审核
     *
     * @param itemId 项目id
     * @param status 状态
     * @param message 提交审核备注
     */
    @PutMapping("/{itemId}/commit")
    @ResponseStatus(HttpStatus.OK)
    public void commitItem(@PathVariable("itemId") long itemId, @RequestBody AuditStatus status, @RequestHeader("comment") String message) {
        itemService.auditItem(itemId, status, message);
    }

    /**
     * 项目批量上架下架
     *
     * @param putData   上下架
     */
    @PutMapping("/enabled")
    @ResponseBody
    public ApiResult enabled(@RequestBody Map<String, Object> putData) {
        final String param = "enabled";
        final String items = "items";
        //失败的个数
        int count = 0;
        List<Long> itemList = (List<Long>)putData.get(items);
        //总个数
        int size = itemList.size();
        if (putData.get(param) != null) {
            if (itemList.size() != 0) {
                for (Long id : itemList){
                    try {
                        itemService.freezeOrEnable(id, (boolean) putData.get(param));
                    }catch(Exception e){
                        count++;
                    }
                }
            }else{
                throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                        , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), items), null));
            }
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), param), null));
        }
        return ApiResult.withOk("总数:"+size +",成功数:"+(size - count)+",失败数:"+count);
    }

    /**
     * 项目批量推荐/取消推荐
     * @param putData
     */
    @PutMapping("/recommended")
    @ResponseBody
    public ApiResult recommended(@RequestBody Map<String, Object> putData){
        final String param = "recommended";
        final String items = "items";
        //失败的个数
        int count = 0;
        List<Long> itemList = (List<Long>)putData.get(items);
        //总个数
        int size = itemList.size();
        if(putData.get(param) != null){
            if(itemList.size() != 0){
                for (Long id : itemList) {
                    itemService.recommended(id,(boolean)putData.get(param));
                }
            }else{
                throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                        , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), items), null));
            }
        }else{
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), param), null));
        }
        return ApiResult.withOk("总数:"+size +",成功数:"+(size - count)+",失败数:"+count);
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
                , FieldBuilder.asName(Item.class, "thumbnailUrl")
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
                            AuditStatus status = (AuditStatus) data;
                            if (status == null)
                                return null;
                            return status.getMessage();
                        })
                        .build()
                , FieldBuilder.asName(Item.class, "enable")
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
            if (queryData.get("enabled") != null) {
                if ((boolean) queryData.get("enabled"))
                    conditions.add(cb.isTrue(root.get(Item_.enable)));
                else
                    conditions.add(cb.isFalse(root.get(Item_.enable)));
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

}
