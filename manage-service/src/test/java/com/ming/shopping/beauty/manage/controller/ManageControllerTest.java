package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.ManageConfigTest;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.model.HttpStatusCustom;
import com.ming.shopping.beauty.service.model.definition.ManagerModel;
import com.ming.shopping.beauty.service.model.request.LoginOrRegisterBody;
import com.ming.shopping.beauty.service.repository.LoginRepository;
import com.ming.shopping.beauty.service.service.InitService;
import com.ming.shopping.beauty.service.service.SystemService;
import me.jiangcai.wx.web.exception.NoWeixinClientException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.NestedServletException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author helloztt
 */
public class ManageControllerTest extends ManageConfigTest {
    @Autowired
    private ConversionService conversionService;
    private static final String manageLogin = "/managerLogin", managerLoginRequest = "/managerLoginRequest", manageLoginResult = "/manageLoginResult";


    @Autowired
    private LoginRepository loginRepository;
    /**
     * 非微信环境无法工作
     */
    @Test(expected = NoWeixinClientException.class)
    public void weixinOnly() throws Throwable {
        try {
            mockMvc.perform(get(manageLogin + "/" + UUID.randomUUID().toString()))
                    .andExpect(status().isOk());
        } catch (NestedServletException nestedServletException) {
            throw nestedServletException.getCause();
        }
    }

    /**
     * 管理员后台扫码的登录过程中有2个参与session
     * 一个session是PC那边的，另一头则是微信这边的
     * 分别取名desktopSession,wechatSession
     */
    @Test
    public void testManageIndex() throws Exception {
        //以CJ的超级管理员账号为测试对象
        nextCurrentWechatAccount();


        //随便找个ID登录，期望提示session失效
        MockHttpSession wechatSession = (MockHttpSession) mockMvc.perform(wechatGet(manageLogin + "/" + random.nextInt(1000)))
                .andExpect(status().is(HttpStatusCustom.SC_SESSION_TIMEOUT))
                .andReturn().getRequest().getSession();

        // 桌面开始搞事情了
        MockHttpSession desktopSession = (MockHttpSession) mockMvc.perform(
                get("/currentManager")
        )
                .andExpect(status().isForbidden())
                .andReturn().getRequest().getSession();
        //去获取一个ID
        MvcResult mvcResult = mockMvc.perform(get(managerLoginRequest)
                .session(desktopSession)
        )
                .andExpect(status().is(HttpStatusCustom.SC_ACCEPTED))
                .andReturn();
        //之后判断登录状态需要用这个session
//        MockHttpSession desktopSession = (MockHttpSession) mvcResult.getRequest().getSession();
        String result = mvcResult.getResponse().getContentAsString();
        String requestId = objectMapper.readTree(result).get("id").asText();
        //再次登录，由于CJ的账号没有openId，期望 HttpStatusCustom.SC_LOGIN_NOT_EXIST
        mockMvc.perform(wechatGet(manageLogin + "/" + requestId).session(wechatSession))
                .andExpect(status().is(HttpStatusCustom.SC_LOGIN_NOT_EXIST));

        //用蒋才的账号登录一下，更新openId
        LoginOrRegisterBody registerBody = new LoginOrRegisterBody();
        registerBody.setMobile(InitService.cjMobile);
        registerBody.setAuthCode("1234");
        mockMvc.perform(makeWechat(post(SystemService.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerBody)))
                .session(wechatSession)
        )
                .andExpect(status().isOk());

        //登录前看看登录情况
        mockMvc.perform(get(manageLoginResult + "/" + requestId)
                .session(desktopSession))
                .andExpect(status().isNoContent());

        //再次登录，期望成功
        mockMvc.perform(wechatGet(manageLogin + "/" + requestId)
                .session(wechatSession)
        )
                .andExpect(status().isOk());

        //看看登录结果
        mockMvc.perform(get(manageLoginResult + "/" + requestId)
                .session(desktopSession))
                .andExpect(status().isOk());

        // 若桌面再次发起登录请求，应该直接给数据，而且协议符合ManagerModal
        mockMvc.perform(
                get(managerLoginRequest)
                        .session(desktopSession)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(matchModel(new ManagerModel(conversionService))))
        ;

        // 同时，应当支持 /currentManager
        mockMvc.perform(
                get("/currentManager")
                        .session(desktopSession)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$").value(matchModel(new ManagerModel(conversionService))))
        ;

        //再试一个没有权限的用户
        Login login = mockLogin();
        mvcResult = mockMvc.perform(wechatGet(managerLoginRequest))
                .andExpect(status().is(HttpStatusCustom.SC_ACCEPTED))
                .andReturn();
        desktopSession = (MockHttpSession) mvcResult.getRequest().getSession();
        result = mvcResult.getResponse().getContentAsString();
        requestId = objectMapper.readTree(result).get("id").asText();
        nextCurrentWechatAccount(login.getWechatUser());

        mockMvc.perform(get(manageLoginResult + "/" + requestId).session(desktopSession)).andExpect(status().isNoContent());

        mockMvc.perform(wechatGet(manageLogin + "/" + requestId))
                .andExpect(status().isForbidden());

        mockMvc.perform(get(manageLoginResult + "/" + requestId).session(desktopSession)).andExpect(status().isNoContent());
    }
/*
    @Test
    public void setManageLevel() throws Exception {
        //以root运行
        Login root = mockRoot();
        updateAllRunWith(root);

        //要设置权限的人
        Login login = mockLogin();

        //随便设置了4个权限
        String manageLevelMessage = "root,rootManager,merchantRoot,merchantManager";
        mockMvc.perform(put("/manage/" + login.getId() + "/manageLevel")
                .content(objectMapper.writeValueAsString(manageLevelMessage))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        //看看是否设置成功了
        Login one = loginRepository.findOne(login.getId());
        one.getLevelSet().contains(ManageLevel.merchantRoot);

        //设置一个权限
        String level = "root";
        mockMvc.perform(put("/manage/" + login.getId() + "/manageLevel")
                .content(objectMapper.writeValueAsString(level))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        one = loginRepository.findOne(login.getId());
        one.getLevelSet().forEach(System.out::println);

        //设置包含重复权限的时候,看看是否正常
        mockMvc.perform(put("/manage/" + login.getId() + "/manageLevel")
                .content(objectMapper.writeValueAsString(manageLevelMessage))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        one = loginRepository.findOne(login.getId());
        //因为设置了4个权限, 如果大于4,或者小于4就有问题
        assertThat(one.getLevelSet().size() == 4);

        //清空权限
        mockMvc.perform(put("/manage/" + login.getId() + "/manageLevel")
                .content(objectMapper.writeValueAsString(null))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        one = loginRepository.findOne(login.getId());
        assertThat(one.getLevelSet().size() == 0);

        //既然设置了商户权限,那就用它运行一下试试
        updateAllRunWith(one);

        Merchant merchant = mockMerchant();
        mockMvc.perform(get("/merchant"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.list[0].name").value((merchant.getName())));
    }*/
}