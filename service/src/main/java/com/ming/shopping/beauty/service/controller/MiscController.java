package com.ming.shopping.beauty.service.controller;

import com.huotu.verification.service.VerificationCodeService;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.service.LoginService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * 杂七杂八的玩意儿
 *
 * @author CJ
 */
@Controller
public class MiscController {
    private static final Log log = LogFactory.getLog(MiscController.class);

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private LoginService loginService;

    @GetMapping(value = "/sendAuthCode/{mobile}")
    @ResponseStatus(HttpStatus.OK)
    public void sendAuthCode(@PathVariable @Valid @Size(min = 11, max = 11, message = "手机号") String mobile
            , BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ApiResultException(
                    //提示 XXX格式错误
                    ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                            , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), bindingResult.getAllErrors().get(0).getDefaultMessage())
                            , null));
        }
        try {
            verificationCodeService.sendCode(mobile, loginService.loginVerificationType());
        } catch (IOException e) {
            log.error("发送验证码失败", e);
            throw new ApiResultException(
                    ApiResult.withCodeAndMessage(ResultCodeEnum.THIRD_ERROR.getCode(), e.getMessage(), null));
        }
    }

}
