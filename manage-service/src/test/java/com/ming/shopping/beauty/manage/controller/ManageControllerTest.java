package com.ming.shopping.beauty.manage.controller;

import com.jayway.jsonpath.JsonPath;
import com.ming.shopping.beauty.manage.ManageConfigTest;
import com.ming.shopping.beauty.service.entity.login.Login;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /**
     * 测试管理流程
     */
    @Test
    public void manageIt() throws Exception {
        Login root = mockRoot();
        updateAllRunWith(root);
        // 查下管理员的数量
        int total = JsonPath.read(mockMvc.perform(
                get("/manage")
        ).andReturn().getResponse().getContentAsString(), "$.pagination.total");
        // 添加一个普通用户之后管理员数量保持不变
        mockLogin();
        mockMvc.perform(
                get("/manage")
        ).andExpect(jsonPath("$.pagination.total").value(total));
        // 添加一个商户之后管理员数量保持不变
        mockMerchant();
        mockMvc.perform(
                get("/manage")
        ).andExpect(jsonPath("$.pagination.total").value(total));
        // 但如果添加的是一个管理员 那就不行了
        int count = 0;
        for (ManageLevel level : Login.rootLevel) {
            count++;
            final int current = total + count;
            mockManager(level);
            mockMvc.perform(
                    get("/manage")
            ).andExpect(jsonPath("$.pagination.total").value(current));
        }

        // 测试更新权限
        Login one = mockLogin();
        assertThat(one.getLevelSet())
                .as("一开始是没权限的")
                .isNullOrEmpty();

        // 确保没有root
        ManageLevel[] targetLevel = null;
        while (targetLevel == null || Arrays.binarySearch(targetLevel, ManageLevel.root) >= 0) {
            targetLevel = randomArray(Login.rootLevel.toArray(new ManageLevel[Login.rootLevel.size()]), 1);
        }

        //
        mockMvc.perform(
                put("/manage/{id}/levelSet", one.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Stream.of(targetLevel).map(Enum::name).collect(Collectors.toList())))
        )
                .andExpect(status().is2xxSuccessful());
        assertThat(loginService.findOne(one.getId()).getLevelSet())
                .as("新的权限符合需求")
                .hasSameElementsAs(Arrays.asList(targetLevel));
        // 再清楚掉
        mockMvc.perform(
                put("/manage/{id}/levelSet", one.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new ArrayList<>()))
        )
                .andExpect(status().is2xxSuccessful());
        assertThat(loginService.findOne(one.getId()).getLevelSet())
                .as("应该没有权限了")
                .isNullOrEmpty();

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