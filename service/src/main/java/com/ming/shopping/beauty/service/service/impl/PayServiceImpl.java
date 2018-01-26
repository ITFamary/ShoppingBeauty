package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.service.PayService;
import me.jiangcai.payment.PayableOrder;
import me.jiangcai.payment.entity.PayOrder;
import me.jiangcai.wx.standard.entity.WeixinPayOrder;
import me.jiangcai.wx.standard.service.WeixinPaymentForm;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author helloztt
 */
@Service
public class PayServiceImpl implements PayService {
    @Override
    public ModelAndView paySuccess(HttpServletRequest request, PayableOrder payableOrder, PayOrder payOrder) {
        return null;
    }

    @Override
    public ModelAndView pay(HttpServletRequest request, PayableOrder order, PayOrder payOrder, Map<String, Object> additionalParameters) {
        if(payOrder instanceof WeixinPayOrder){
            WeixinPayOrder weixinPayOrder = (WeixinPayOrder) payOrder;
        }
        return null;
    }

    @Override
    public boolean isPaySuccess(String id) {
        return false;
    }

    @Override
    public PayableOrder getOrder(String id) {
        return null;
    }
}
