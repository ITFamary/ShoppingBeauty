package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Login_;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.login.Store_;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.service.StoreService;
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
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lxf
 */
@Controller
@RequestMapping("/store")
public class ManageStoreController extends AbstractCrudController<Store, Long> {

    @Autowired
    private StoreService storeService;
    /**
     * 门店列表
     *
     * @param queryData 查询参数
     * @return
     */
    @Override
    @RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
    @PreAuthorize("hasAnyRole('ROOT', '" + Login.ROLE_STORE_ROOT + "' ,'"+Login.ROLE_MERCHANT_ROOT+"')")
    public RowDefinition<Store> list(@RequestBody(required= false)Map<String, Object> queryData) {
        return super.list(queryData);
    }

    /**
     * 新增门店
     *
     * @param postData 门店信息
     * @param otherData 其他信息
     * @return
     * @throws URISyntaxException
     */
    @Override
    @PreAuthorize("hassAnyRole('ROOT','"+Login.ROLE_MERCHANT_ROOT+"')")
    public ResponseEntity addOne(@RequestBody Store postData,@RequestBody Map<String, Object> otherData) throws URISyntaxException {
        final String loginId = "loginId";
        final String merchantId = "merchanId";
        if(otherData.get(loginId) == null){
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), loginId), null));
        }
        if(otherData.get(merchantId) == null){
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), merchantId), null));
        }
        Store store = storeService.addStore((long) otherData.get(loginId), (long) otherData.get(merchantId),
                postData.getName(), postData.getTelephone(), postData.getContact(), postData.getAddress());
        return ResponseEntity.created(new URI("/store/" + store.getId()))
                .build();
    }

    /**
     * 启用/禁用 门店
     *
     * @param loginId
     * @param putData
     */
    @PutMapping("/{storeId}")
    @PreAuthorize("hassAnyRole('ROOT','"+Login.ROLE_MERCHANT_ROOT+"')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setEnable(@PathVariable("storeId") long loginId, Map<String, Boolean> putData) {
        final String param = "enable";
        if (putData.get(param) != null) {
            storeService.freezeOrEnable(loginId, putData.get(param));
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), param), null));
        }
    }

    /**
     * 门店操作员列表
     *
     * @param storeId
     * @return
     */
    @GetMapping("/{storeId}/manage")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "','"+Login.ROLE_STORE_ROOT+"')")
    @RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
    public RowDefinition<Store> listForManage(@PathVariable long storeId) {
        return new RowDefinition<Store>() {
            @Override
            public Class<Store> entityClass() {
                return Store.class;
            }

            @Override
            public List<FieldDefinition<Store>> fields() {
                return listFieldsForManage();
            }

            @Override
            public Specification<Store> specification() {
                return listSpecificationForManage(storeId);
            }
        };
    }

    /**
     * 门店操作员详情
     *
     * @param manageId
     * @return
     */
    @GetMapping("/{storeId}/manage/{manageId}")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "','"+Login.ROLE_STORE_ROOT+"')")
    public String manageDetail(@PathVariable long manageId) {
        return "redirect:/login/" + manageId;
    }

    /**
     * 新增门店操作员
     *
     * @param storeId
     * @param manageId
     */
    @PostMapping("/{storeId}/manage/{manageId}")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "','"+Login.ROLE_STORE_ROOT+"')")
    public void addMerchantManage(@PathVariable long storeId, @PathVariable long manageId) {
        storeService.addStore(manageId, storeId);
    }

    /**
     * 启用/禁用商户操作员
     *
     * @param storeId
     * @param manageId
     * @param putData
     */
    @PutMapping("/{storeId}/manage/{manageId}")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "','"+Login.ROLE_STORE_ROOT+"')")
    public void enableMerchantManage(@PathVariable long storeId, @PathVariable long manageId, Map<String, Boolean> putData) {
        final String param = "enable";
        if (putData.get(param) != null) {
            storeService.freezeOrEnable(manageId, putData.get(param));
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), param), null));
        }
    }

    @Override
    protected List<FieldDefinition<Store>> listFields() {
        return Arrays.asList(
                FieldBuilder.asName(Store.class, "id")
                        .build()
                , FieldBuilder.asName(Store.class, "username")
                        .addSelect(storeRoot -> storeRoot.join(Store_.login, JoinType.LEFT).get(Login_.loginName))
                        .build()
                , FieldBuilder.asName(Store.class, "name")
                        .build()
                , FieldBuilder.asName(Store.class, "telephone")
                        .build()
                , FieldBuilder.asName(Store.class, "contact")
                        .build()
                , FieldBuilder.asName(Store.class, "address")
                        //TODO 地址问题
                        .addFormat((data, type) -> data.toString())
                        .build()
                , FieldBuilder.asName(Store.class, "enabled")
                        .build()
                , FieldBuilder.asName(Store.class, "createTime")
                        .build()

        );
    }

    @Override
    protected Specification<Store> listSpecification(Map<String, Object> queryData) {
        return (root, cq, cb) -> {
            List<Predicate> conditionList = new ArrayList<>();
            if (queryData.get("username") != null) {
                conditionList.add(cb.equal(root.join(Store_.login).get(Login_.loginName), queryData.get("username")));
            }
            return cb.and(conditionList.toArray(new Predicate[conditionList.size()]));
        };
    }

    protected List<FieldDefinition<Store>> listFieldsForManage() {
        return Arrays.asList(
                FieldBuilder.asName(Store.class, "id")
                        .addSelect(merchantRoot -> merchantRoot.get(Store_.id))
                        .build()
                , FieldBuilder.asName(Store.class, "userName")
                        .addSelect(merchantRoot -> merchantRoot.join(Store_.login, JoinType.LEFT).get(Login_.loginName))
                        .build()
                , FieldBuilder.asName(Store.class, "enabled")
                        .addSelect(merchantRoot -> merchantRoot.get(Store_.enabled))
                        .build()
                , FieldBuilder.asName(Store.class, "level")
                        .addSelect(merchantRoot -> merchantRoot.join(Store_.login, JoinType.LEFT).get(Login_.levelSet))
                        .addFormat((data, type) -> {
                            Set<ManageLevel> levelSet = (Set<ManageLevel>) data;
                            return levelSet.stream().map(ManageLevel::title).collect(Collectors.joining(","));
                        })
                        .build()
                , FieldBuilder.asName(Store.class, "createtime")
                        .addSelect(merchantRoot -> merchantRoot.get(Store_.createTime))
                        .build()
        );
    }

    protected Specification<Store> listSpecificationForManage(long storeId) {
        return (root, cq, cb) ->
                cb.and(
                        cb.isFalse(root.get(Store_.manageable))
                        , cb.equal(root.join(Store_.store, JoinType.LEFT).get(Store_.id), storeId)
                );
    }

    @Override
    @PreAuthorize("denyAll()")
    public void deleteOne(Long aLong) {
        super.deleteOne(aLong);
    }

    @Override
    @PreAuthorize("denyAll()")
    public RowDefinition<Store> getDetail(Long aLong) {
        return null;
    }


}
