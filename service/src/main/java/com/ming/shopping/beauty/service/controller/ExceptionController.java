package com.ming.shopping.beauty.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
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
    public ModelAndView noHandlerFound(ApiResultException exception, HttpServletRequest request, HttpServletResponse response) throws IOException {
        //如果没有返回提示，获取原始message
        String errorMsg;
        if (exception.getApiResult() == null) {
            errorMsg = HttpStatus.valueOf(exception.getHttpStatus()).getReasonPhrase();
        } else {
            errorMsg = exception.getApiResult().getMessage();
        }
        if(isAjaxRequestOrBackJson(request)){
            response.sendError(exception.getHttpStatus(),errorMsg);
            response.getOutputStream().print(objectMapper.writeValueAsString(exception.getApiResult()));
            return null;
        }else{
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("/views/error");
            modelAndView.addObject("status", exception.getHttpStatus());
            modelAndView.addObject("exception", errorMsg);
            return modelAndView;
        }
    }

    /***
     * 判断当前请求是否是ajax请求或者是返回json格式字符串
     * @param request
     * @return
     */
    private boolean isAjaxRequestOrBackJson(HttpServletRequest request) {
        String accept = request.getHeader("accept");
        String x_request_with = request.getHeader("X-Requested-With");
        if (!StringUtils.isEmpty(accept) && accept.toLowerCase().contains("application/json")) return true;
        if (!StringUtils.isEmpty(x_request_with) && x_request_with.toLowerCase().contains("xmlhttprequest"))
            return true;
        return false;
    }
}
