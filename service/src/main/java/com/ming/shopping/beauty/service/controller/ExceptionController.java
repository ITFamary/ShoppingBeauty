package com.ming.shopping.beauty.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 定义一些异常的处理
 * Created by helloztt on 2018/1/4.
 */
@ControllerAdvice
public class ExceptionController {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(ApiResultException.class)
    @ResponseBody
    public ApiResult noHandlerFound(ApiResultException exception, HttpServletResponse response) throws IOException {
        //如果没有返回提示，获取原始message
        if (exception.getApiResult() == null) {
            response.sendError(exception.getHttpStatus(), HttpStatus.valueOf(exception.getHttpStatus()).getReasonPhrase());
            return null;
        } else {
            response.sendError(exception.getHttpStatus(), exception.getApiResult().getMessage());
            return exception.getApiResult();
        }
    }
}
