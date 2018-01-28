package com.ming.shopping.beauty.service.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.LoginRequest;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.definition.ManagerModel;
import com.ming.shopping.beauty.service.model.request.LoginOrRegisterBody;
import com.ming.shopping.beauty.service.service.LoginRequestService;
import com.ming.shopping.beauty.service.service.LoginService;
import com.ming.shopping.beauty.service.service.SystemService;
import com.ming.shopping.beauty.service.utils.LoginAuthentication;
import me.jiangcai.crud.row.RowService;
import me.jiangcai.wx.OpenId;
import me.jiangcai.wx.model.WeixinUserDetail;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

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
    public Object managerLoginRequest(@AuthenticationPrincipal Object principal, HttpServletRequest request, HttpServletResponse response) {
        //判断是否登录
        String sessionId = request.getSession().getId();
        final Map<String, Object> result;
        if (principal instanceof String) {
            LoginRequest loginRequest = loginRequestService.newRequest(sessionId);
            response.setStatus(HttpStatusCustom.SC_ACCEPTED);
            String text = systemService.toUrl("/managerLogin/" + loginRequest.getId());
            result = new HashMap<>();
            result.put("id", loginRequest.getId());
            result.put("url", text);
        } else if (principal instanceof Login) {
            Login login = (Login) principal;
            result = RowService.drawEntityToRow(login, new ManagerModel(conversionService).getDefinitions(), null);
            response.setStatus(HttpStatusCustom.SC_OK);
        } else
            throw new IllegalStateException("这是什么登录情况：" + principal);
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
            loginToSecurity(login, request, response); // TODO 微信端扫码用户还需要再次登录么？
            loginRequestService.login(requestId, login);
            response.setStatus(HttpStatusCustom.SC_OK);
        }
    }

    @GetMapping("/currentManager")
    @ResponseBody
    public Object currentManager(@AuthenticationPrincipal Object principal, HttpServletResponse response) {
        if (principal instanceof String) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        } else {
            return RowService.drawEntityToRow(principal, new ManagerModel(conversionService).getDefinitions(), null);
        }
    }

    /**
     * 管理登录结果
     */
    @GetMapping("/manageLoginResult/{requestId}")
    @ResponseBody
    public Object manageLoginResult(@PathVariable long requestId, HttpServletRequest request, HttpServletResponse response) {
        LoginRequest loginRequest = loginRequestService.findOne(requestId);
        if (loginRequest == null) {
            //session已失效，请重新获取二维码
            response.setStatus(HttpStatusCustom.SC_SESSION_TIMEOUT);
            return null;
        } else if (loginRequest.getLogin() == null) {
            //登录尚未被获准
            response.setStatus(HttpStatusCustom.SC_NO_CONTENT);
            return null;
        } else {
            loginToSecurity(loginRequest.getLogin(), request, response);
            response.setStatus(HttpStatusCustom.SC_OK);
            return RowService.drawEntityToRow(loginRequest.getLogin(), new ManagerModel(conversionService).getDefinitions(), null);
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
