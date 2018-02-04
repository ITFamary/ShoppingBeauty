package com.ming.shopping.beauty.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    private static final Log log = LogFactory.getLog(ExceptionController.class);

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
        response.sendError(exception.getHttpStatus(),errorMsg);
        if(isAjaxRequestOrBackJson(request)){
            response.getWriter().write(objectMapper.writeValueAsString(exception.getApiResult()));
            return null;
        }else{
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("/views/error");
            modelAndView.addObject("status", exception.getHttpStatus());
            modelAndView.addObject("message", errorMsg);
            return modelAndView;
        }
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public void needLogin(@AuthenticationPrincipal Login login,AccessDeniedException exception
            ,HttpServletResponse response) throws IOException {
        if(!login.getLevelSet().contains(ManageLevel.user)){
            response.sendError(HttpStatusCustom.SC_LOGIN_NOT_EXIST);
        }
    }

    /***
     * 判断当前请求是否是ajax请求或者是返回json格式字符串
     * @param request
     * @return
     */
    private boolean isAjaxRequestOrBackJson(HttpServletRequest request) {
        String accept = request.getHeader("accept");
        if (!StringUtils.isEmpty(accept) && accept.toLowerCase().contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) return false;
        return true;
    }
}
