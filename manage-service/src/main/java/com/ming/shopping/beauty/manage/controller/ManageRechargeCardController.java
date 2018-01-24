package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.service.LoginService;
import com.ming.shopping.beauty.service.service.RechargeCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import java.text.MessageFormat;

/**
 * @author lxf
 */
@Controller
public class ManageRechargeCardController {

    @Autowired
    private LoginService loginService;
    @Autowired
    private RechargeCardService rechargeCardService;

    /**
     * 批量生成充值卡
     *
     * @param login       操作员
     * @param guideUserId 推荐者
     * @param num         生成的数量
     */
    @PostMapping("/recharge/{guideUserId}")
    public ApiResult massProduction(@AuthenticationPrincipal Login login, long guideUserId, int num) {
        final Login one = loginService.findOne(guideUserId);
        //生成失败的数量
        int count = 0;
        if (one != null) {
            //生成卡
            rechargeCardService.newCard(num, guideUserId, login.getId());
        } else
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), guideUserId), null));
        return ApiResult.withOk("总数:" + num + ",成功数:" + (num - count) + ",失败数:" + count);
    }


}
