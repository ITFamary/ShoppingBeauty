package com.ming.shopping.beauty.controller;

import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 定义一些异常的处理
 * Created by helloztt on 2018/1/4.
 */
@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(ApiResultException.class)
    @ResponseBody
    public ApiResult noHandlerFound(ApiResultException exception){
        return exception.getApiResult();
    }
}
