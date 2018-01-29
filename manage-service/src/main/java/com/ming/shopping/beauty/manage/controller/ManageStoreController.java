package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.service.entity.login.*;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.service.RepresentService;
import com.ming.shopping.beauty.service.service.StoreService;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.supplier.AntDesignPaginationDramatizer;
import me.jiangcai.crud.row.supplier.SingleRowDramatizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author lxf
 */
@Controller
@RequestMapping("/store")
public class ManageStoreController extends AbstractCrudController<Store, Long> {

    @Autowired
    private StoreService storeService;
    @Autowired
    private RepresentService representService;

    /**
     * 门店列表
     *
     * @param queryData 查询参数
     * @return
     */
    @Override
    @RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "')")
    public RowDefinition<Store> list(Map<String, Object> queryData) {
        return super.list(queryData);
    }

    /**
     * 新增门店
     *
     * @param postData  门店信息
     * @param otherData 其他信息
     * @return
     * @throws URISyntaxException
     */
    @Override
    @PostMapping
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity addOne(@RequestBody Store postData, @RequestBody Map<String, Object> otherData) throws URISyntaxException {
        final String loginId = "loginId";
        final String merchantId = "merchantId";
        if (otherData.get(loginId) == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), loginId), null));
        }
        if (otherData.get(merchantId) == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), merchantId), null));
        }
        Store store = storeService.addStore(Long.parseLong(otherData.get(loginId).toString()), Long.parseLong(otherData.get(merchantId).toString()),
                postData.getName(), postData.getTelephone(), postData.getContact(), postData.getAddress());
        return ResponseEntity.created(new URI("/store/" + store.getId()))
                .build();
    }

    @Override
    @GetMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "')")
    @RowCustom(distinct = true, dramatizer = SingleRowDramatizer.class)
    public RowDefinition<Store> getOne(@PathVariable("storeId") Long storeId) {
        return new RowDefinition<Store>() {
            @Override
            public Class<Store> entityClass() {
                return Store.class;
            }

            @Override
            public List<FieldDefinition<Store>> fields() {
                return Arrays.asList(
                        FieldBuilder.asName(Store.class, "id")
                                .build()
                        , FieldBuilder.asName(Store.class, "name")
                                .build()
                        , FieldBuilder.asName(Store.class, "telephone")
                                .build()
                        , FieldBuilder.asName(Store.class, "contact")
                                .build()
                        , FieldBuilder.asName(Store.class,"represents")
                                .build()
                );
            }

            @Override
            public Specification<Store> specification() {
                return (root, query, cb) ->
                    cb.equal(root.get(Store_.id),storeId);
            }
        };
    }

    /**
     * 启用/禁用 门店
     *
     * @param loginId
     * @param enable
     */
    @PutMapping("/{storeId}/enabled")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setEnable(@PathVariable("storeId") long loginId,@RequestBody Boolean enable) {
        if (enable != null) {
            storeService.freezeOrEnable(loginId, enable);
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), enable), null));
        }
    }

    /**
     * 门店代表列表
     *
     * @param storeId 门店id
     * @return
     */
    @GetMapping("/{storeId}/represent")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "','" + Login.ROLE_STORE_ROOT + "')")
    @RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
    public RowDefinition<Represent> listForRepresent(@PathVariable long storeId) {
        return new RowDefinition<Represent>() {
            @Override
            public Class<Represent> entityClass() {
                return Represent.class;
            }

            @Override
            public List<Order> defaultOrder(CriteriaBuilder criteriaBuilder, Root<Represent> root) {
                return Arrays.asList(
                        criteriaBuilder.asc(root.get("enable"))
                        , criteriaBuilder.desc(root.get("createTime"))
                );
            }

            @Override
            public List<FieldDefinition<Represent>> fields() {
                return listFieldsForRepresent();
            }

            @Override
            public Specification<Represent> specification() {
                return listSpecificationForRepresent(storeId);
            }
        };
    }

    /**
     * 添加门店代表
     *
     * @param storeId     门店
     * @param representId 用户id
     */
    @PostMapping("/{storeId}/represent/{representId}")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "','" + Login.ROLE_STORE_ROOT + "')")
    @ResponseStatus(HttpStatus.CREATED)
    public void addRepresent(@PathVariable(required = true) long storeId, @PathVariable(required = true) long representId) {
        representService.addRepresent(representId, storeId);
    }

    /**
     * 启用禁用门店代表
     *
     * @param representId 门店代表id
     * @param enable
     */
    @PutMapping("/{storeId}/represent/{representId}/enabled")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "','" + Login.ROLE_STORE_ROOT + "')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enableRepresent(@PathVariable(required = true) long representId, @RequestBody Boolean enable) {
        if (enable != null) {
            representService.freezeOrEnable(representId, enable);
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), enable), null));
        }
    }

    /**
     * 移除角色和门店代表的关联
     * @param storeId
     * @param representId
     */
    @DeleteMapping("/{storeId}/represent/{representId}")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "','" + Login.ROLE_STORE_ROOT + "')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRepresent(@PathVariable("storeId")long storeId,@PathVariable("representId") long representId){
        representService.removerRepresent(storeId,representId);
    }

    //门店

    @Override
    protected List<FieldDefinition<Store>> listFields() {
        return Arrays.asList(
                FieldBuilder.asName(Store.class, "storeId")
                        .addSelect(storeRoot -> storeRoot.get(Store_.id))
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
//                , FieldBuilder.asName(Store.class, "address")
//                        //TODO 地址问题
//                        .addFormat((data, type) -> data.toString())
//                        .build()
                , FieldBuilder.asName(Store.class, "enabled")
                        .build()
                , FieldBuilder.asName(Store.class, "createTime")
                        .addFormat((data,type)->{
                            return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(((LocalDateTime) data));
                        })
                        .build()

        );
    }

    @Override
    protected Specification<Store> listSpecification(Map<String, Object> queryData) {
        return (root, cq, cb) -> {
            List<Predicate> conditionList = new ArrayList<>();
            if(queryData.get("telephone") != null){
                conditionList.add(cb.equal(root.join(Store_.telephone),queryData.get("telephone")));
            }
            if (queryData.get("username") != null) {
                conditionList.add(cb.equal(root.join(Store_.login).get(Login_.loginName), queryData.get("username")));
            }
            return cb.and(conditionList.toArray(new Predicate[conditionList.size()]));
        };
    }

    //门店代表

    private List<FieldDefinition<Represent>> listFieldsForRepresent() {
        return Arrays.asList(
                FieldBuilder.asName(Represent.class, "id").build()
                , FieldBuilder.asName(Represent.class, "username")
                        .addSelect(representRoot -> representRoot.join(Represent_.login, JoinType.LEFT).get(Login_.nickName))
                        .build()
                , FieldBuilder.asName(Represent.class, "mobile")
                        .addSelect(representRoot -> representRoot.join(Represent_.login, JoinType.LEFT).get(Login_.loginName))
                        .build()
                , FieldBuilder.asName(Represent.class, "createTime")
                        .addFormat((data, type) -> {
                            return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(((LocalDateTime) data));
                        })
                        .build()
                //TODO 应该还有一个业绩相关的.
        );
    }

    private Specification<Represent> listSpecificationForRepresent(long storeId) {
        return (root, cq, cb) -> cb.equal(root.join(Represent_.store).get(Store_.id), storeId);
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
