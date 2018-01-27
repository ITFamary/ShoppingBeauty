package com.ming.shopping.beauty.client.controller;

import com.ming.shopping.beauty.service.entity.log.CapitalFlow;
import com.ming.shopping.beauty.service.entity.log.CapitalFlow_;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.order.RechargeOrder;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.request.DepositBody;
import com.ming.shopping.beauty.service.service.RechargeCardService;
import com.ming.shopping.beauty.service.service.RechargeOrderService;
import me.jiangcai.crud.row.DefaultRowDramatizer;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.supplier.AntDesignPaginationDramatizer;
import me.jiangcai.lib.sys.service.SystemStringService;
import me.jiangcai.payment.entity.PayOrder;
import me.jiangcai.payment.exception.SystemMaintainException;
import me.jiangcai.payment.service.PaymentService;
import me.jiangcai.wx.standard.service.WeixinPaymentForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author helloztt
 */
@Controller
@RequestMapping("/capital")
public class CapitalController {
    @Autowired
    private ConversionService conversionService;
    @Autowired
    private RechargeCardService rechargeCardService;
    @Autowired
    private RechargeOrderService rechargeOrderService;
    @Autowired
    private SystemStringService systemStringService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private WeixinPaymentForm weixinPaymentForm;

    @GetMapping("/flow")
    @RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
    public RowDefinition<CapitalFlow> capitalFlow(@AuthenticationPrincipal Login login) {
        return new RowDefinition<CapitalFlow>() {
            @Override
            public Class<CapitalFlow> entityClass() {
                return CapitalFlow.class;
            }

            @Override
            public List<Order> defaultOrder(CriteriaBuilder criteriaBuilder, Root<CapitalFlow> root) {
                return Arrays.asList(
                        criteriaBuilder.desc(root.get(CapitalFlow_.id))
                );
            }

            @Override
            public List<FieldDefinition<CapitalFlow>> fields() {
                return listFields();
            }

            @Override
            public Specification<CapitalFlow> specification() {
                return (root, cq, cb) ->
                        cb.equal(root.get(CapitalFlow_.userId), login.getId());
            }
        };
    }

    @PostMapping(value = "/deposit", produces = "application/x-www-form-urlencoded")
    public ModelAndView deposit(@AuthenticationPrincipal Login login, @Valid @RequestBody DepositBody postData
            , BindingResult bindingResult, HttpServletRequest request, HttpServletResponse response) throws SystemMaintainException {
        if (bindingResult.hasErrors()) {
            throw new ApiResultException(
                    //提示 XXX格式错误
                    ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                            , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), bindingResult.getAllErrors().get(0).getDefaultMessage())
                            , null));
        }
        if (postData.getDepositSum() != null) {
            Integer minAmount = systemStringService.getCustomSystemString("shopping.service.recharge.min.amount", null, true, Integer.class, 500);
            if (postData.getDepositSum().compareTo(BigDecimal.valueOf(minAmount)) == -1) {
                throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.RECHARGE_MONEY_NOT_ENOUGH.getCode()
                        , MessageFormat.format(ResultCodeEnum.RECHARGE_MONEY_NOT_ENOUGH.getMessage(), minAmount.toString()), null));
            }
            //先新增一个支付订单
            RechargeOrder rechargeOrder = rechargeOrderService.newOrder(login.getUser(), postData.getDepositSum());
            //走支付流程
            Map<String,Object> additionalParam = new HashMap<>(1);
            additionalParam.put("redirectUrl",postData.getRedirectUrl());
            return paymentService.startPay(request, rechargeOrder, weixinPaymentForm, additionalParam);
        } else if (!StringUtils.isEmpty(postData.getCdKey())) {
            //使用充值卡
            rechargeCardService.useCard(postData.getCdKey(), login.getId());
        } else {
            throw new ApiResultException((ApiResult.withError(ResultCodeEnum.NO_MONEY_CARD)));
        }
        ModelAndView model = new ModelAndView();
        model.setViewName("success");
        model.addObject("redirectUrl",postData.getRedirectUrl());
        return model;
    }

    private List<FieldDefinition<CapitalFlow>> listFields() {
        return Arrays.asList(
                FieldBuilder.asName(CapitalFlow.class, "time")
                        .addSelect(root -> root.get(CapitalFlow_.happenTime))
                        .addFormat((data, type) -> conversionService.convert(data, String.class))
                        .build()
                , FieldBuilder.asName(CapitalFlow.class, "title")
                        .addSelect(root -> root.get(CapitalFlow_.flowType))
                        .addFormat((data, type) -> data.toString())
                        .build()
                , FieldBuilder.asName(CapitalFlow.class, "sum")
                        .addSelect(root -> root.get(CapitalFlow_.changed))
                        .addFormat((data, type) -> conversionService.convert(data, String.class))
                        .build()
                , FieldBuilder.asName(CapitalFlow.class, "type")
                        .addSelect(root -> root.get(CapitalFlow_.flowType))
                        .addFormat((data, type) -> ((Enum) data).ordinal())
                        .build()
                , FieldBuilder.asName(CapitalFlow.class, "orderId")
                        .addSelect(root -> root.get(CapitalFlow_.id))
                        .build()
        );
    }
}
