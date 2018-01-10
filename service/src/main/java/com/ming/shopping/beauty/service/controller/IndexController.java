package com.ming.shopping.beauty.service.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.request.LoginOrRegisterBody;
import com.ming.shopping.beauty.service.service.LoginService;
import com.ming.shopping.beauty.service.utils.Constant;
import com.ming.shopping.beauty.service.utils.LoginAuthentication;
import me.jiangcai.wx.OpenId;
import me.jiangcai.wx.model.WeixinUserDetail;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.text.MessageFormat;

/**
 * 注册前的校验、注册、登录、发送验证码等
 *
 * @author helloztt
 */
@Controller
public class IndexController {

    private static final Log log = LogFactory.getLog(IndexController.class);
    private final SecurityContextRepository httpSessionSecurityContextRepository
            = new HttpSessionSecurityContextRepository();
    @Autowired
    private LoginService loginService;

    @GetMapping(value = Constant.TO_LOGIN)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public void toLogin() {
    }

    /**
     * 检查用户是否已经注册，返回200
     * <p>若已注册，在 {@link ApiResult#data} 中返回登录名</p>
     * <p>若未注册，则返回空</p>
     *
     * @param openId 微信返回的openId
     * @return
     */
    @GetMapping(value = "/isExist")
    @ResponseBody
    public ApiResult isExist(@OpenId String openId) {
        Login login = loginService.asWechat(openId);
        if (login == null) {
            return ApiResult.withOk();
        } else {
            return ApiResult.withOk(login.getLoginName());
        }
    }

    /**
     * 检查这个手机号是否已被注册
     * <p>未被注册，返回{@link HttpStatusCustom#SC_OK}={@code 200}</p>
     * <p>已被注册，返回{@link HttpStatusCustom#SC_EXPECTATION_FAILED}={@code 417}</p>
     *
     * @param mobile 手机号
     * @return
     */
    @GetMapping(value = "/isRegister/{mobile}")
    @ResponseBody
    public ApiResult isRegister(@PathVariable String mobile) {
        loginService.mobileVerify(mobile);
        return ApiResult.withOk();
    }

    /**
     * 注册登录接口
     *
     * @param weixinUserDetail
     * @param postData
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = Constant.LOGIN)
    @ResponseStatus(HttpStatus.OK)
    public void login(WeixinUserDetail weixinUserDetail, @Valid @RequestBody LoginOrRegisterBody postData, BindingResult bindingResult
            , HttpServletRequest request, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            throw new ApiResultException(
                    //提示 XXX格式错误
                    ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                            , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), bindingResult.getAllErrors().get(0).getDefaultMessage())
                            , null));
        }
        Login login = loginService.getLogin(weixinUserDetail.getOpenId(), postData.getMobile(), postData.getAuthCode()
                , postData.getSurname(), postData.getGender(), postData.getCdKey(), postData.getGuideUserId());
        //注册或登录成功了，加到 security 中
        loginToSecurity(login, request, response);
    }

    private void loginToSecurity(Login login, HttpServletRequest request, HttpServletResponse response) {
        //对 login 执行登录

        HttpRequestResponseHolder holder = new HttpRequestResponseHolder(request, response);
        SecurityContext context = httpSessionSecurityContextRepository.loadContext(holder);

        LoginAuthentication authentication = new LoginAuthentication(login.getId(), loginService);
        context.setAuthentication(authentication);
//
        SecurityContextHolder.getContext().setAuthentication(authentication);

        httpSessionSecurityContextRepository.saveContext(context, holder.getRequest(), holder.getResponse());
    }


}
