package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Login_;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Merchant_;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.service.MerchantService;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.supplier.AntDesignPaginationDramatizer;
import me.jiangcai.crud.row.supplier.SingleRowDramatizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
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
 * @author helloztt
 */
@Controller
@RequestMapping("/merchant")
public class ManageMerchantController extends AbstractCrudController<Merchant, Long> {
    @Autowired
    private MerchantService merchantService;

    @Autowired
    private ConversionService conversionService;

    /**
     * 商户列表
     *
     * @param queryData 查询参数
     * @return
     */
    @RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
    @PreAuthorize("hasAnyRole('ROOT')")
    @Override
    public RowDefinition<Merchant> list(Map<String, Object> queryData) {
        return super.list(queryData);
    }


    /**
     * 新增商户
     *
     * @param postData  商户信息
     * @param otherData 其他信息
     * @return
     * @throws URISyntaxException
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ROOT')")
    @Override
    public ResponseEntity addOne(@RequestBody Merchant postData, @RequestBody Map<String, Object> otherData) throws URISyntaxException {
        final String param = "loginId";
        if (otherData.get(param) == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), param), null));
        }
        if (StringUtils.isEmpty(postData.getName()) || StringUtils.isEmpty(postData.getTelephone())
                || StringUtils.isEmpty(postData.getContact())) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "请求数据"), null));
        }
        Merchant merchant = merchantService.addMerchant(Long.parseLong(otherData.get(param).toString()), postData);
        return ResponseEntity
                .created(new URI("/merchant/" + merchant.getId()))
                .build();
    }

    @Override
    @GetMapping("/{merchantId}")
    @PreAuthorize("hasAnyRole('ROOT')")
    @RowCustom(distinct = true, dramatizer = SingleRowDramatizer.class)
    public RowDefinition<Merchant> getOne(@PathVariable("merchantId") Long merchantId) {
        return new RowDefinition<Merchant>() {

            @Override
            public Class<Merchant> entityClass() {
                return Merchant.class;
            }

            @Override
            public List<FieldDefinition<Merchant>> fields() {
                return Arrays.asList(
                        FieldBuilder.asName(Merchant.class, "id")
                                .build()
                        , FieldBuilder.asName(Merchant.class, "name")
                                .build()
                        , FieldBuilder.asName(Merchant.class, "telephone")
                                .build()
                        , FieldBuilder.asName(Merchant.class, "contact")
                                .build()
                        , FieldBuilder.asName(Merchant.class, "enabled")
                                .build()
                        , FieldBuilder.asName(Merchant.class, "stores")
                                .build()
                );
            }

            @Override
            public Specification<Merchant> specification() {
                return ((root, query, cb) -> cb.equal(root.get(Merchant_.id), merchantId));
            }
        };
    }

    /**
     * 启用/禁用 商户
     *
     * @param loginId
     * @param enable
     */
    @PutMapping("/{merchantId}/enabled")
    @PreAuthorize("hasAnyRole('ROOT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setEnable(@PathVariable("merchantId") long loginId, @RequestBody Boolean enable) {
        if (enable != null) {
            merchantService.freezeOrEnable(loginId, enable);
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), enable), null));
        }
    }

    /**
     * 商户操作员列表
     *
     * @param merchantId
     * @return
     */
    @GetMapping("/{merchantId}/manage")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "')")
    @RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
    public RowDefinition<Merchant> listForManage(@PathVariable long merchantId) {
        return new RowDefinition<Merchant>() {
            @Override
            public Class<Merchant> entityClass() {
                return Merchant.class;
            }

            @Override
            public List<FieldDefinition<Merchant>> fields() {
                return listFieldsForManage();
            }

            @Override
            public Specification<Merchant> specification() {
                return listSpecificationForManage(merchantId);
            }
        };
    }

    /**
     * 商户操作员详情
     *
     * @param manageId
     * @return
     */
    @GetMapping("/{merchantId}/manage/{manageId}")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "')")
    public String manageDetail(@PathVariable long manageId) {
        return "redirect:/login/" + manageId;
    }

    /**
     * 新增商户操作员
     *
     * @param merchantId
     * @param manageId
     */
    @PostMapping("/{merchantId}/manage/{manageId}")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "')")
    @ResponseStatus(HttpStatus.CREATED)
    public void addMerchantManage(@PathVariable long merchantId, @PathVariable long manageId) {
        merchantService.addMerchant(manageId, merchantId);
    }

    /**
     * 启用/禁用商户操作员
     *
     * @param merchantId
     * @param manageId
     * @param enable
     */
    @PutMapping("/{merchantId}/manage/{manageId}")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enableMerchantManage(@PathVariable long merchantId, @PathVariable long manageId, @RequestBody Boolean enable) {
        if (enable != null) {
            merchantService.freezeOrEnable(manageId, enable);
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), enable), null));
        }
    }

    @Override
    protected List<FieldDefinition<Merchant>> listFields() {
        return Arrays.asList(
                FieldBuilder.asName(Merchant.class, "merchantId")
                        .addSelect(merchantRoot -> merchantRoot.get(Merchant_.id))
                        .build()
                , FieldBuilder.asName(Merchant.class, "username")
                        .addSelect(merchantRoot -> merchantRoot.join(Merchant_.login, JoinType.LEFT).get(Login_.loginName))
                        .build()
                , FieldBuilder.asName(Merchant.class, "name")
                        .addSelect(merchantRoot -> merchantRoot.get(Merchant_.name))
                        .build()
                , FieldBuilder.asName(Merchant.class, "contact")
                        .addSelect(merchantRoot -> merchantRoot.get(Merchant_.contact))
                        .build()
                , FieldBuilder.asName(Merchant.class, "telephone")
                        .addSelect(merchantRoot -> merchantRoot.get(Merchant_.telephone))
                        .build()
                , FieldBuilder.asName(Merchant.class, "address")
                        // TODO: 2018/1/18
                        .addSelect(merchantRoot -> merchantRoot.get(Merchant_.address))
                        .build()
                , FieldBuilder.asName(Merchant.class, "enabled")
                        .addSelect(merchantRoot -> merchantRoot.get(Merchant_.enabled))
                        .build()
                , FieldBuilder.asName(Merchant.class, "createTime")
                        .addSelect(merchantRoot -> merchantRoot.get(Merchant_.createTime))
                        .addFormat((data, type) -> conversionService.convert(data, String.class))
                        .build()
        );
    }

    @Override
    protected Specification<Merchant> listSpecification(Map<String, Object> queryData) {
        return (root, cq, cb) -> {
            List<Predicate> conditionList = new ArrayList<>();
            conditionList.add(cb.isTrue(root.get(Merchant_.manageable)));
            if (queryData.get("username") != null) {
                conditionList.add(cb.equal(root.join(Merchant_.login).get(Login_.loginName), queryData.get("username")));
            }
            return cb.and(conditionList.toArray(new Predicate[conditionList.size()]));
        };
    }

    protected List<FieldDefinition<Merchant>> listFieldsForManage() {
        return Arrays.asList(
                FieldBuilder.asName(Merchant.class, "id")
                        .addSelect(merchantRoot -> merchantRoot.get(Merchant_.id))
                        .build()
                , FieldBuilder.asName(Merchant.class, "username")
                        .addSelect(merchantRoot -> merchantRoot.join(Merchant_.login, JoinType.LEFT).get(Login_.loginName))
                        .build()
                , FieldBuilder.asName(Merchant.class, "enabled")
                        .addSelect(merchantRoot -> merchantRoot.get(Merchant_.enabled))
                        .build()
                //TODO ManageLevel的问题
                /*, FieldBuilder.asName(Merchant.class, "level")
                        .addSelect(merchantRoot -> merchantRoot.join(Merchant_.login, JoinType.LEFT).get(Login_.levelSet))
                        .addFormat((data, type) -> {
                            Set<ManageLevel> levelSet = (Set<ManageLevel>) data;
                            return levelSet.stream().map(ManageLevel::title).collect(Collectors.joining(","));
                        })
                        .build()*/
                , FieldBuilder.asName(Merchant.class, "createTime")
                        .addSelect(merchantRoot -> merchantRoot.get(Merchant_.createTime))
                        .addFormat((data, type) -> {
                            String format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(((LocalDateTime) data));
                            return format;
                        })
                        .build()
        );
    }

    protected Specification<Merchant> listSpecificationForManage(long merchantId) {
        return (root, cq, cb) ->
                cb.and(
                        cb.isFalse(root.get(Merchant_.manageable))
                        , cb.equal(root.join(Merchant_.merchant, JoinType.LEFT).get(Merchant_.id), merchantId)
                );
    }

}
