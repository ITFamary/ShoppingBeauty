package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Merchant_;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.order.MainOrder_;
import com.ming.shopping.beauty.service.entity.settlement.SettlementSheet;
import com.ming.shopping.beauty.service.entity.settlement.SettlementSheet_;
import com.ming.shopping.beauty.service.entity.support.SettlementStatus;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.service.MerchantService;
import com.ming.shopping.beauty.service.service.settlement.SettlementSheetService;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.supplier.AntDesignPaginationDramatizer;
import me.jiangcai.crud.utils.MapUtils;
import me.jiangcai.lib.sys.SystemStringConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
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
@RequestMapping("/settlementSheet")
@Controller
@RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
@PreAuthorize("hasAnyRole('ROOT','" + SystemStringConfig.MANAGER_ROLE + "','" + Login.ROLE_PLATFORM_SETTLEMENT + "')")
public class ManageSettlementSheetController extends AbstractCrudController<SettlementSheet, Long, SettlementSheet> {

    @Autowired
    private SettlementSheetService settlementSheetService;
    @Autowired
    private MerchantService merchantService;

    /**
     * 结算单
     *
     * @param request 请求
     * @return
     */
    @Override
    public RowDefinition<SettlementSheet> list(HttpServletRequest request) {
        Map<String, Object> queryData = MapUtils.changeIt(request.getParameterMap());
        return new RowDefinition<SettlementSheet>() {
            @Override
            public CriteriaQuery<SettlementSheet> dataGroup(CriteriaBuilder cb, CriteriaQuery<SettlementSheet> query, Root<SettlementSheet> root) {
                return query.groupBy(root);
            }

            @Override
            public List<Order> defaultOrder(CriteriaBuilder criteriaBuilder, Root<SettlementSheet> root) {
                return listOrder(criteriaBuilder, root);
            }

            @Override
            public Class<SettlementSheet> entityClass() {
                return SettlementSheet.class;
            }

            @Override
            public List<FieldDefinition<SettlementSheet>> fields() {
                return listFields();
            }

            @Override
            public Specification<SettlementSheet> specification() {
                return listSpecification(queryData);
            }
        };
    }

    /**
     * 提交申请
     *
     * @param id
     * @param comment
     */
    @PutMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "','" + Login.ROLE_MERCHANT_SETTLEMENT + "')")
    public void submitSheet(@PathVariable("id") Long id, @RequestBody String comment) {
        if (id == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "结算单id"), null));
        }
        SettlementSheet sheet = settlementSheetService.findSheet(id);
        settlementSheetService.submitSheet(sheet, comment);
    }

    /**
     * 同意申请,未打款
     *
     * @param id
     * @param comment
     */
    @PutMapping("/{id}/approval")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_PLATFORM_SETTLEMENT + "')")
    public void approvalSheet(@PathVariable("id") Long id, @RequestBody String comment) {
        if (id == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "结算单id"), null));
        }
        SettlementSheet sheet = settlementSheetService.findSheet(id);
        settlementSheetService.approvalSheet(sheet, comment);
    }

    /**
     * 打回申请
     *
     * @param id
     * @param comment
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_PLATFORM_SETTLEMENT + "')")
    public void rejectSheet(@PathVariable("id") Long id, @RequestBody String comment) {
        if (id == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "结算单id"), null));
        }
        SettlementSheet sheet = settlementSheetService.findSheet(id);
        settlementSheetService.rejectSheet(sheet, comment);
    }

    /**
     * 撤销结算单
     *
     * @param id
     */
    @PutMapping("/{id}/revoke")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "')")
    public void revokeSheet(@PathVariable("id") Long id) {
        if (id == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "结算单id"), null));
        }
        SettlementSheet sheet = settlementSheetService.findSheet(id);
        settlementSheetService.revokeSheet(sheet);
    }

    /**
     * 已支付结算款
     *
     * @param id
     */
    @PutMapping("/{id}/already")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_PLATFORM_SETTLEMENT + "')")
    public void alreadySheet(@PathVariable("id") Long id, @RequestBody BigDecimal actualAmount) {
        if (id == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "结算单id"), null));
        }
        SettlementSheet sheet = settlementSheetService.findSheet(id);
        settlementSheetService.alreadyPaid(sheet, actualAmount);
    }

    /**
     * 商户收到结算款,确认
     *
     * @param id
     */
    @PutMapping("{id}/complete")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "')")
    public void complete(@PathVariable("id") Long id) {
        if (id == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "结算单id"), null));
        }
        SettlementSheet sheet = settlementSheetService.findSheet(id);
        settlementSheetService.completeSheet(sheet);
    }

    /**
     * 产生一个结算单,由商户发起,统计系统规定的时间周期内的订单的MainOrder的信息.
     *
     * @param merchantId 商户id
     * @return
     * @throws URISyntaxException
     */
    @PostMapping("/{merchantId}")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity productSheet(@PathVariable("merchantId") Long merchantId) throws URISyntaxException {
        Merchant merchant = merchantService.findMerchant(merchantId);
        SettlementSheet settlementSheet = settlementSheetService.addSheet(merchant);
        return ResponseEntity
                .created(new URI("/settlementSheet/" + settlementSheet.getId() + "/store"))
                .build();
    }

    /**
     * 结算单中根据门店划分的明细
     *
     * @param id 结算单id
     * @return
     */
    @GetMapping("/{id}/store")
    public RowDefinition<MainOrder> getDetailForStore(@PathVariable("id") Long id) {
        return new RowDefinition<MainOrder>() {
            @Override
            public Class<MainOrder> entityClass() {
                return MainOrder.class;
            }

            @Override
            public CriteriaQuery<MainOrder> dataGroup(CriteriaBuilder cb, CriteriaQuery<MainOrder> query, Root<MainOrder> root) {
                return query.groupBy(
                        root.get(MainOrder_.store)
                );
            }

            @Override
            public List<FieldDefinition<MainOrder>> fields() {
                return Arrays.asList(
                        //结算金额
                        FieldBuilder.asName(MainOrder.class, "settlementAmount")
                                .addBiSelect((mainOrderRoot, criteriaBuilder) -> criteriaBuilder.sum(mainOrderRoot.get(MainOrder_.settlementAmount)))
                                .build()
                        , FieldBuilder.asName(MainOrder.class, "actualAmount")
                                .addBiSelect((mainOrderRoot, criteriaBuilder) -> criteriaBuilder.sum(mainOrderRoot.get(MainOrder_.finalAmount)))
                                .build()
                        , FieldBuilder
                                .asName(MainOrder.class, "count")
                                .addBiSelect((mainOrderRoot, criteriaBuilder) -> criteriaBuilder.count(mainOrderRoot))
                                .build()
                        , FieldBuilder
                                .asName(MainOrder.class, "storeName")
                                .addSelect(mainOrderRoot -> mainOrderRoot.get(MainOrder_.store))
                                .build()
                );
            }

            @Override
            public Specification<MainOrder> specification() {
                return (root, query, cb) -> cb.and(
                        cb.lessThan(root.get(MainOrder_.payTime), LocalDateTime.now().minusDays(7))
                        , cb.equal(root.join(MainOrder_.settlementSheet).get(SettlementSheet_.id), id));
            }
        };
    }

    @Override
    protected List<Order> listOrder(CriteriaBuilder criteriaBuilder, Root<SettlementSheet> root) {
        return Arrays.asList(
                criteriaBuilder.desc(root.get(SettlementSheet_.createTime))
        );
    }

    @Override
    protected List<FieldDefinition<SettlementSheet>> listFields() {

        return Arrays.asList(
                FieldBuilder.asName(SettlementSheet.class, "id")
                        .build()
                , FieldBuilder.asName(SettlementSheet.class, "merchantName")
                        .addSelect(settlementSheetRoot -> settlementSheetRoot.join(SettlementSheet_.merchant).get(Merchant_.name))
                        .build()
                , FieldBuilder.asName(SettlementSheet.class, "actualAmount")
                        .build()
                , FieldBuilder.asName(SettlementSheet.class, "createTime")
                        .addFormat((data, type) -> {
                            return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(((LocalDateTime) data));
                        })
                        .build()
                , FieldBuilder.asName(SettlementSheet.class, "settlementAmount")
                        .addBiSelect((settlementSheetRoot, criteriaBuilder) -> criteriaBuilder.sum(settlementSheetRoot.get("mainOrderSet").get("settlementAmount")))
                        .build()
                , FieldBuilder.asName(SettlementSheet.class, "status")
                        .addSelect(settlementSheetRoot -> settlementSheetRoot.get(SettlementSheet_.settlementStatus))
                        .addFormat((data, type) -> data.toString())
                        .build()
                , FieldBuilder.asName(SettlementSheet.class, "comment")
                        .build()
                , FieldBuilder.asName(SettlementSheet.class, "transferTime")
                        .build()
        );
    }

    @Override
    protected Specification<SettlementSheet> listSpecification(Map<String, Object> queryData) {
        return (root, query, cb) -> {
            List<Predicate> queryList = new ArrayList<>();
            if (queryData.get("id") != null) {
                queryList.add(cb.equal(root.get(SettlementSheet_.id), queryData.get("id")));
            }
            if (queryData.get("status") != null) {
                queryList.add(cb.equal(root.get(SettlementSheet_.settlementStatus),
                        SettlementStatus.valueOf(queryData.get("status").toString())));
            }
            return cb.and(queryList.toArray(new Predicate[queryList.size()]));
        };
    }

    @Override
    @PreAuthorize("denyAll()")
    public void deleteOne(Long aLong) {
        super.deleteOne(aLong);
    }

}
