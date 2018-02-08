package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.ManageConfigTest;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.repository.LoginRepository;
import com.ming.shopping.beauty.service.service.InitService;
import org.hamcrest.core.StringStartsWith;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author helloztt
 */
//@ActiveProfiles(ServiceConfig.PROFILE_MYSQL)
public class ManageLoginControllerTest extends ManageConfigTest {
    public static final String BASE_URL = "/login";

    @Autowired
    private LoginRepository loginRepository;

    @Test
    public void getOne() throws Exception {
        //来个管理员
        Login rootLogin = mockRoot();
        updateAllRunWith(rootLogin);

        Login mockLogin = mockLogin();
        mockMvc.perform(get(BASE_URL + "/" + mockLogin.getId()))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id").value(mockLogin.getId()))
                .andExpect(jsonPath("$.name").value(StringStartsWith.startsWith(mockLogin.getUser().getFamilyName())))
                .andExpect(jsonPath("$.mobile").value(mockLogin.getLoginName()))
                .andExpect(jsonPath("$.enabled").value(mockLogin.isEnabled()))
                .andExpect(jsonPath("$.balance").value(BigDecimal.ZERO));
//                .andExpect(jsonPath("$.consumption").value(BigDecimal.ZERO));

        //条件查询
        //如果是空字符串的时候, 查询所有
        mockLogin();
        mockLogin();
        mockLogin.setEnabled(false);
        loginRepository.save(mockLogin);

        //默认1个, 我创建了4个 5个   mobile是空的,当查询所有
        int size = loginRepository.findAll().size();
        mockMvc.perform(get(BASE_URL)
                .param("mobile"," "))
                .andDo(print())
                .andExpect(jsonPath("$.pagination.total").value(size));

        mockMvc.perform(get(BASE_URL)
                .param("mobile",mockLogin.getLoginName()))
                .andDo(print())
                .andExpect(jsonPath("$.pagination.total").value(1));

        mockMvc.perform(get(BASE_URL)
                .param("enabled","false"))
                .andDo(print())
                .andExpect(jsonPath("$.pagination.total").value(1));
    }

    @Test
    public void listByMobile() throws Exception {
        updateAllRunWith(mockRoot());
        mockMvc.perform(
                get("/login?mobile={mobile}", InitService.cjMobile.substring(1))
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(jsonPath("$.list.length()").value(1));
    }


    @Test
    public void setGuidable() throws Exception {
        Login login = mockLogin();
        Login root = mockRoot();
        updateAllRunWith(root);
        //可以推荐他人
        mockMvc.perform(put("/login/" + login.getId() + "/guidable")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(true)))
                .andDo(print())
                .andReturn();
        Login one = loginRepository.findOne(login.getId());
        //一定是禁用的
        assertThat(one.isGuidable()).isTrue();
        //启用
        mockMvc.perform(put("/login/" + login.getId() + "/guidable")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(false)))
                .andDo(print())
                .andReturn();
        Login two = loginRepository.findOne(login.getId());
        //一定是启用的
        assertThat(two.isGuidable()).isFalse();
    }

    @Test
    public void setEnable() throws Exception {
        Login login = mockLogin();
        Login root = mockRoot();
        updateAllRunWith(root);
        //禁用
        mockMvc.perform(put("/login/" + login.getId() + "/enabled")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(false)))
                .andDo(print())
                .andReturn();
        Login one = loginRepository.findOne(login.getId());
        //一定是禁用的
        assertThat(one.isEnabled()).isFalse();
        //启用
        mockMvc.perform(put("/login/" + login.getId() + "/enabled")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(true)))
                .andDo(print())
                .andReturn();
        Login two = loginRepository.findOne(login.getId());
        //一定是启用的
        assertThat(two.isEnabled()).isTrue();
    }


    @Test
    public void balanceTest() throws Exception {
        Login login = mockLogin();
        Login root = mockRoot();
        updateAllRunWith(root);

        //现在查询他的余额应该是0
        String contentAsString = mockMvc.perform(get("/login/{id}/balance", login.getId()))
                .andDo(print())
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        assertThat(new BigDecimal(contentAsString).equals(BigDecimal.ZERO)).isTrue();

        //通过管理员后台给他充值
        Map<String, Object> postData = new HashMap<>();
        postData.put("mobile", login.getLoginName());
        BigDecimal b5000 = new BigDecimal("5000.00");
        postData.put("amount", b5000);
        mockMvc.perform(post("/manage/manualRecharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postData)))
                .andDo(print())
                .andExpect(status().isOk());

        //查询login的余额
        String amount = mockMvc.perform(get("/login/{id}/balance", login.getId()))
                .andDo(print())
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();


        BigDecimal bAmount = new BigDecimal(amount);

        assertThat(bAmount.equals(b5000)).isTrue();
        Login one = loginRepository.getOne(login.getId());
        //保证他的余额一直是0
        assertThat(one.getUser().getCurrentAmount().equals(BigDecimal.ZERO)).isTrue();

        //通过管理员扣款

        BigDecimal b2000 = new BigDecimal("2000.00");
        postData.put("amount", b2000);
        mockMvc.perform(post("/manage/deduction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postData)))
                .andDo(print())
                .andExpect(status().isOk());

        //查询他的余额
        String amountSub = mockMvc.perform(get("/login/{id}/balance", login.getId()))
                .andDo(print())
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        BigDecimal bSub = new BigDecimal(amountSub);
        assertThat(bSub.equals(b5000.subtract(b2000))).isTrue();

        one = loginRepository.getOne(login.getId());
        //保证他的余额一直是0
        assertThat(one.getUser().getCurrentAmount().equals(BigDecimal.ZERO)).isTrue();

        //清空余额
        postData.put("amount", null);
        mockMvc.perform(post("/manage/deduction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postData)))
                .andDo(print())
                .andExpect(status().isOk());
        //再次查询应该是0了

        String zero = mockMvc.perform(get("/login/{id}/balance", login.getId()))
                .andDo(print())
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        BigDecimal bZero = new BigDecimal(zero);
        assertThat(bZero.equals(new BigDecimal("0.00"))).isTrue();

        one = loginRepository.getOne(login.getId());
        //保证他的余额一直是0
        assertThat(one.getUser().getCurrentAmount().equals(BigDecimal.ZERO)).isTrue();
    }

}