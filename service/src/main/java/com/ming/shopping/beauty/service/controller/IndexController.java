package com.ming.shopping.beauty.service.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.LoginRequest;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.request.LoginOrRegisterBody;
import com.ming.shopping.beauty.service.service.LoginRequestService;
import com.ming.shopping.beauty.service.service.LoginService;
import com.ming.shopping.beauty.service.service.SystemService;
import com.ming.shopping.beauty.service.utils.LoginAuthentication;
import me.jiangcai.wx.OpenId;
import me.jiangcai.wx.model.WeixinUserDetail;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

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
    @Autowired
    private LoginRequestService loginRequestService;
    @Autowired
    private SystemService systemService;
    @Autowired
    private QRController qrController;
    @Autowired
    private ConversionService conversionService;

    @GetMapping(value = SystemService.TO_LOGIN)
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
    @PostMapping(value = SystemService.LOGIN)
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

    /**
     * 管理登录申请
     *
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/managerLoginRequest")
    @ResponseBody
    public Object managerLoginRequest(HttpServletRequest request, HttpServletResponse response) {
        //判断是否登录
        String sessionId = request.getSession().getId();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            LoginRequest loginRequest = loginRequestService.newRequest(sessionId);
            response.setStatus(HttpStatusCustom.SC_ACCEPTED);
            String text = systemService.toUrl("/managerLogin/" + sessionId);
            result.put("id", loginRequest.getId());
            result.put("url", text);
        } else if (authentication instanceof LoginAuthentication) {
            Login login = (Login) authentication.getPrincipal();
            result.put("id", login.getId());
            result.put("username", authentication.getName());
            result.put("nickname", login.getNickName());
            result.put("enabled", true);
            result.put("authorities", authentication.getAuthorities().toArray());
            if (login.getMerchant() != null) {
                result.put("merchantId", login.getMerchant().getMerchantId());
            }
            if (login.getStore() != null) {
                result.put("storeId", login.getStore().getStoreId());
            }
            result.put("createTime", conversionService.convert(login.getCreateTime(), String.class));
            response.setStatus(HttpStatusCustom.SC_OK);
        }
        return result;
    }

    /**
     * 扫码登录
     *
     * @param weixinUserDetail
     */
    @GetMapping("/managerLogin/{requestId}")
    @ResponseBody
    public void manageLogin(WeixinUserDetail weixinUserDetail, @PathVariable long requestId, HttpServletRequest request, HttpServletResponse response) {
        if (loginRequestService.findOne(requestId) == null) {
            //session已失效，请重新获取二维码
            response.setStatus(HttpStatusCustom.SC_SESSION_TIMEOUT);
            return;
        }
        Login login = loginService.asWechat(weixinUserDetail.getOpenId());
        if (login == null) {
            //说明没有这个用户，让他先去注册或登录
            response.setStatus(HttpStatusCustom.SC_LOGIN_NOT_EXIST);
        } else if (CollectionUtils.isEmpty(login.getLevelSet()) || !login.isEnabled()) {
            //说明用户没有权限登录管理后台
            response.setStatus(HttpStatusCustom.SC_FORBIDDEN);
//            loginRequestService.remove(requestId);
        } else {
            //执行登录
            loginToSecurity(login, request, response);
            loginRequestService.login(requestId, login);
            response.setStatus(HttpStatusCustom.SC_OK);
        }
    }

    /**
     * 管理登录结果
     *
     * @param requestId
     * @param response
     */
    @GetMapping("/manageLoginResult/{requestId}")
    @ResponseBody
    public void manageLoginResult(@PathVariable long requestId, HttpServletResponse response) {
        LoginRequest loginRequest = loginRequestService.findOne(requestId);
        if (loginRequest == null) {
            //session已失效，请重新获取二维码
            response.setStatus(HttpStatusCustom.SC_SESSION_TIMEOUT);
        } else if (loginRequest.getLogin() == null) {
            //登录尚未被获准
            response.setStatus(HttpStatusCustom.SC_NO_CONTENT);
        } else {
            response.setStatus(HttpStatusCustom.SC_OK);
        }
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
