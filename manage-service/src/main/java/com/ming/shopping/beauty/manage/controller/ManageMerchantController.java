package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Login_;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Merchant_;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.repository.MerchantRepository;
import com.ming.shopping.beauty.service.service.MerchantService;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.*;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.supplier.AntDesignPaginationDramatizer;
import me.jiangcai.crud.utils.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;

import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author helloztt
 */
@Controller
@RequestMapping("/merchant")
public class ManageMerchantController extends AbstractCrudController<Merchant, Long> {
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private ConversionService conversionService;

    /**
     * 商户列表
     *
     * @param request
     * @return
     */
    @RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
    @PreAuthorize("hasAnyRole('ROOT')")
    @Override
    public RowDefinition<Merchant> list(HttpServletRequest request) {
        return super.list(request);
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

    /**
     * 编辑商户信息.
     *
     * @param postData
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "')")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMerchant(@AuthenticationPrincipal Login login, @RequestBody Merchant postData) {
        if (postData.getId() == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), postData), null));
        }
        if (login.getLevelSet().contains(ManageLevel.root)) {
            //说明是超管 可以修改所有的对象
            updating(postData);
        } else {
            //仅仅是商户自身
            if (login.getId().equals(postData.getId())) {
                updating(postData);
            } else
                throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                        , MessageFormat.format(ResultCodeEnum.LOGIN_NOT_MANAGE.getMessage(), login), null));
        }
    }

    private void updating(Merchant postData) {
        Merchant merchant = merchantRepository.findOne(postData.getId());
        if (postData.getName() != null) {
            merchant.setName(postData.getName());
        }
        if (postData.getTelephone() != null) {
            merchant.setTelephone(postData.getTelephone());
        }
        if (postData.getContact() != null) {
            merchant.setContact(postData.getContact());
        }
        merchantRepository.save(merchant);
    }

    @Override
    protected Object describeEntity(Merchant origin) {
        return RowService.drawEntityToRow(origin, Arrays.asList(
                FieldBuilder.asName(Merchant.class, "id")
                        .build()
                , FieldBuilder.asName(Merchant.class, "name")
                        .build()
                , FieldBuilder.asName(Merchant.class, "telephone")
                        .build()
                , FieldBuilder.asName(Merchant.class, "contact")
                        .build()
        ), null);
    }

    /**
     * 商户详情
     *
     * @param aLong
     * @return
     */
    @Override
    @PreAuthorize("hasAnyRole('ROOT')")
    @ResponseBody
    @GetMapping({"/{id}"})
    public Object getOne(@PathVariable("id") Long aLong) {
        return super.getOne(aLong);
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
    public RowDefinition listForManage(@PathVariable long merchantId) throws IOException {
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
//        merchantService.addMerchant(manageId, merchantId);
        throw new NoSuchMethodError("尚未实现，缺少必要的权限字段");
    }

    /**
     * 启用/禁用商户操作员
     *
     * @param merchantId
     * @param manageId
     * @param enable
     */
    @PutMapping("/{merchantId}/manage/{manageId}/enabled")
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
                FieldBuilder.asName(Merchant.class, "id")
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
                        .addSelect(merchantRoot -> merchantRoot.get(Merchant_.address))
                        .addFormat((data, type) -> data != null ? data.toString() : null)
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
                new ManageField() {

                    @Override
                    public Selection<?> select(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> query, Root<Merchant> root) {
                        return root;
                    }

                    @Override
                    protected Object export(Merchant manage, Function<List, ?> exportMe) {
                        return manage.getId();
                    }

                    @Override
                    public Expression<?> order(Root<Merchant> root, CriteriaBuilder criteriaBuilder) {
                        return root.get(Merchant_.id);
                    }

                    @Override
                    public String name() {
                        return "id";
                    }
                },
                new ManageField() {
                    @Override
                    protected Object export(Merchant manage, Function<List, ?> exportMe) {
                        return manage.getLogin().getLoginName();
                    }

                    @Override
                    public String name() {
                        return "username";
                    }
                },
                new ManageField() {
                    @Override
                    protected Object export(Merchant manage, Function<List, ?> exportMe) {
                        return manage.isEnabled();
                    }

                    @Override
                    public String name() {
                        return "enabled";
                    }
                },
                new ManageField() {
                    @Override
                    protected Object export(Merchant manage, Function<List, ?> exportMe) {
                        Set<ManageLevel> levelSet = manage.getLogin().getLevelSet();
                        if (CollectionUtils.isEmpty(levelSet)) {
                            return null;
                        }
                        return levelSet.stream().map(ManageLevel::title).collect(Collectors.joining(","));
                    }

                    @Override
                    public String name() {
                        return "level";
                    }
                },
                new ManageField() {
                    @Override
                    protected Object export(Merchant manage, Function<List, ?> exportMe) {
                        return conversionService.convert(manage.getCreateTime(), String.class);
                    }

                    @Override
                    public String name() {
                        return "createTime";
                    }
                }
        );
    }

    protected Specification<Merchant> listSpecificationForManage(long merchantId) {
        return (root, cq, cb) ->
                cb.and(
                        cb.isFalse(root.get(Merchant_.manageable))
                        , cb.equal(root.join(Merchant_.merchant, JoinType.LEFT).get(Merchant_.id), merchantId)
                );
    }

    @Override
    protected List<Order> listOrder(CriteriaBuilder criteriaBuilder, Root<Merchant> root) {
        return Arrays.asList(
                criteriaBuilder.desc(root.get(Merchant_.id))
        );
    }

    @Override
    @PreAuthorize("denyAll()")
    public void deleteOne(Long aLong) {
        super.deleteOne(aLong);
    }

    @Override
    @PreAuthorize("denyAll()")
    public RowDefinition<Merchant> getDetail(Long aLong) {
        return null;
    }

    private abstract class ManageField implements FieldDefinition<Merchant> {
        @Override
        public Selection<?> select(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> query, Root<Merchant> root) {
            return null;
        }

        @Override
        public Object export(Object origin, MediaType mediaType, Function<List, ?> exportMe) {
            return export((Merchant) origin, exportMe);
        }

        protected abstract Object export(Merchant manage, Function<List, ?> exportMe);

        @Override
        public Expression<?> order(Root<Merchant> root, CriteriaBuilder criteriaBuilder) {
            return null;
        }
    }
}
