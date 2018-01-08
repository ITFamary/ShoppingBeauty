package com.ming.shopping.beauty.client.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.service.LoginService;
import me.jiangcai.wx.OpenId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author helloztt
 */
@Controller
public class ClientIndexController {
    @Autowired
    private LoginService loginService;

    @RequestMapping(value = {"", "/"})
    @ResponseBody
    public String indexTest() {
        return "client index";
    }

    /**
     * 检查用户是否已经注册，返回200
     * <p>若已注册，在 {@link ApiResult#data} 中返回登录名</p>
     * <p>若未注册，则返回空</p>
     *
     * @param openId
     * @return
     */
    @RequestMapping("/isExist")
    @ResponseBody
    public ApiResult isExist(@OpenId String openId) {
        Login login = loginService.asWechat(openId);
        if (login == null) {
            return ApiResult.withOk();
        } else {
            return ApiResult.withOk(login.getLoginName());
        }
    }
}
