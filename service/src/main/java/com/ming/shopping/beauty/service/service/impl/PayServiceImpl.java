package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.order.RechargeOrder;
import com.ming.shopping.beauty.service.entity.support.OrderStatus;
import com.ming.shopping.beauty.service.repository.RechargeOrderRepository;
import com.ming.shopping.beauty.service.service.PayService;
import me.jiangcai.payment.PayableOrder;
import me.jiangcai.payment.entity.PayOrder;
import me.jiangcai.payment.event.OrderPayCancellation;
import me.jiangcai.payment.event.OrderPaySuccess;
import me.jiangcai.wx.standard.entity.WeixinPayOrder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author helloztt
 */
@Service
public class PayServiceImpl implements PayService {
    private static final Log log = LogFactory.getLog(PayServiceImpl.class);

    @Autowired
    private RechargeOrderRepository rechargeOrderRepository;

    @Override
    public ModelAndView paySuccess(HttpServletRequest request, PayableOrder payableOrder, PayOrder payOrder) {
        if (payOrder instanceof WeixinPayOrder) {
            return new ModelAndView("redirect:" + ((WeixinPayOrder) payOrder).getRedirectUrl());
        } else {
            throw new IllegalStateException("暂时不支持：" + payableOrder);
        }
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public ModelAndView pay(HttpServletRequest request, PayableOrder order, PayOrder payOrder, Map<String, Object> additionalParameters) {
        ModelAndView modelAndView = new ModelAndView();
        if (payOrder instanceof WeixinPayOrder) {
            WeixinPayOrder weixinPayOrder = (WeixinPayOrder) payOrder;
            weixinPayOrder.setRedirectUrl(additionalParameters.get("redirectUrl").toString());
            modelAndView.setViewName("paying");
            modelAndView.addObject("title", order.getOrderBody());
            if (!StringUtils.isEmpty(weixinPayOrder.getJavascriptToPay())) {
                //公众号H5支付
                modelAndView.addObject("payJs", weixinPayOrder.getJavascriptToPay());
            } else if (!StringUtils.isEmpty(weixinPayOrder.getCodeUrl())) {
                //微信扫码支付
                modelAndView.addObject("codeUrl", weixinPayOrder.getCodeUrl());
            }
        }
        return modelAndView;
    }

    @Override
    public boolean isPaySuccess(String id) {
        return false;
    }

    @Override
    public PayableOrder getOrder(String id) {
        return null;
    }

    @Override
    public void paySuccess(OrderPaySuccess event) {
        log.info("处理付款成功事件");
        if (event.getPayableOrder() instanceof RechargeOrder) {
            RechargeOrder rechargeOrder = (RechargeOrder) event.getPayableOrder();
            if (rechargeOrder.isPay()) {
                log.warn("订单已支付，却发起了重复事件。" + rechargeOrder.getSerialId());
                return;
            }
//                throw new IllegalStateException("订单已支付");
            rechargeOrder.setPayTime(LocalDateTime.now());
            rechargeOrder.setOrderStatus(OrderStatus.success);
        }
    }

    @Override
    public void payCancel(OrderPayCancellation event) {
        log.info(event.getPayableOrder() + "放弃了支付");
        rechargeOrderRepository.delete(event.getPayOrder().getId());
    }
}
