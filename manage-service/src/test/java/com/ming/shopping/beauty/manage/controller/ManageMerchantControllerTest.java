package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.ManageConfigTest;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.model.request.NewMerchantBody;
import com.ming.shopping.beauty.service.service.LoginService;
import com.ming.shopping.beauty.service.service.MerchantService;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.core.StringStartsWith;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author lxf
 */
public class ManageMerchantControllerTest extends ManageConfigTest {

    @Autowired
    private MerchantService merchantService;
    @Autowired
    private LoginService loginService;

    @Test
    public void merchantList() throws Exception {
        //首先是不具有管理员权限的人访问,拒绝访问
        //随便来个人只要不是管理员
        Login fackManage = mockLogin();
        //身份运行
        updateAllRunWith(fackManage);
        mockMvc.perform(get("/merchant"))
                .andExpect(status().isForbidden());

        //来个管理员
        Login rootlogin = mockRoot();
        updateAllRunWith(rootlogin);
        //生成一个商户
        Merchant merchant = mockMerchant();
        Long id = merchant.getId();
        //查看商户列表看是否有这个商户
        MvcResult mvcResult = mockMvc.perform(get("/merchant"))
                .andExpect(status().isOk())
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        //有这个id的
        assertThat(id).isEqualTo(objectMapper.readTree(contentAsString).get("list").get(0).get("loginId").asLong());
        //再添加一个商户
        Merchant merchant1 = mockMerchant();
        Long id1 = merchant1.getId();

        //所有创建的商户id
        List<Long> oldlist = new ArrayList<>();
        oldlist.add(id);
        oldlist.add(id1);
        //查看商户列表,是否两个商户都存在
        MvcResult mvcResult1 = mockMvc.perform(get("/merchant")).andExpect(status().isOk()).andReturn();
        String contentAsString1 = mvcResult1.getResponse().getContentAsString();

        //获取的商户列表所有id
        List<Long> idList = new ArrayList<>();
        idList.add(objectMapper.readTree(contentAsString1).get("list").get(0).get("loginId").asLong());
        idList.add(objectMapper.readTree(contentAsString1).get("list").get(1).get("loginId").asLong());

        //他们应该是一样的
        assertThat(idList.containsAll(oldlist)).isEqualTo(true);

    }

    @Test
    public void add() throws Exception {
        //来个管理员
        Login rootlogin = mockRoot();
        updateAllRunWith(rootlogin);
        Login willMerchant = mockLogin();
        NewMerchantBody merchantBody = new NewMerchantBody();
        merchantBody.setContact("王女士");
        merchantBody.setLoginId(willMerchant.getId());
        merchantBody.setName("测试商户");
        merchantBody.setTelephone("18477283321");

        String merchantCreationUri = mockMvc.perform(post("/merchant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(merchantBody)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse().getHeader("Location");

        //访问详情
        MvcResult mvcResult = mockMvc.perform(get(merchantCreationUri))
                .andDo(print())
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        //启用
        enableMerchant(willMerchant.getId());

        Merchant one = merchantService.findOne(willMerchant.getId());
        assertThat(one != null).isTrue();
        //禁用 TODO 写的太绕, 这里merchantService.findOne(merchantId) 如果获取不到直接抛异常,我不知道断言能否确定一段代码一定出异常.
        freeMerchant(willMerchant.getId());
        Login login = loginService.findOne(willMerchant.getId());
        Merchant merchant = login.getMerchant();
        assertThat(merchant.isEnabled()).isFalse();
    }

    @Test
    public void merchantManageList() throws Exception {
        //添加一个商户
        Merchant merchant = mockMerchant();
        //启用商户
        Login root = mockRoot();
        updateAllRunWith(root);
        enableMerchant(merchant.getId());

        //以商户身份运行
        updateAllRunWith(merchant.getLogin());
        //添加一个商户管理员
        Merchant merchantManage = mockMerchantManager(merchant.getId());
        //再来一个
        Merchant merchantManage1 = mockMerchantManager(merchant.getId());
        MvcResult mvcResult = mockMvc.perform(get("/merchant/" + merchant.getId() + "/manage"))
                .andExpect(status().isOk())
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<Long> oldList = new ArrayList<>();
        oldList.add(merchantManage.getId());
        oldList.add(merchantManage1.getId());

        //获取的商户列表所有id
        List<Long> idList = new ArrayList<>();
        idList.add(objectMapper.readTree(contentAsString).get("list").get(0).get("id").asLong());
        idList.add(objectMapper.readTree(contentAsString).get("list").get(1).get("id").asLong());

        //他们应该是一样的
        assertThat(idList.containsAll(oldList)).isEqualTo(true);
    }

    @Test
    public void addMerchantManage() throws Exception {
        //添加门店操作员
        //添加一个商户
        Merchant merchant = mockMerchant();
        //启用商户
        Login root = mockRoot();
        updateAllRunWith(root);
        enableMerchant(merchant.getId());
        //以商户身份运行
        updateAllRunWith(merchant.getLogin());
        //要成为操作员的人
        Login willMerchantManage = mockLogin();
        //添加商户管理员
        mockMvc.perform(post("/merchant/" + merchant.getId() + "/manage/" + willMerchantManage.getId()))
                .andExpect(status().isCreated());
        Merchant manage = merchantService.findOne(willMerchantManage.getId());
        assertThat(manage != null).isTrue();
        //禁用操作员
        boolean enable = false;
        mockMvc.perform(put("/merchant/" + merchant.getId() + "/manage/" + willMerchantManage.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enable)))
                .andExpect(status().isNoContent());
        Login login = loginService.findOne(willMerchantManage.getId());
        Merchant merchantManage = login.getMerchant();
        //没被启用
        assertThat(merchantManage.isEnabled())
                .isFalse();

        //查看详情
        manageDetail(willMerchantManage.getId(), merchant.getMerchantId());
    }

    private void manageDetail(long id, long merchantId) throws Exception {
//        mockMvc.perform(get("/merchant/"+merchantId+"/manage/"+id))
//                .andDo(print())
//                .andReturn();
//        mockMvc.perform(get("/login/"+id)).andDo(print()).andExpect(status().isOk());
    }

    private void enableMerchant(long id) throws Exception {
        //现在启用商户.
        boolean enable = true;
        mockMvc.perform(put("/merchant/" + id)
                .content(objectMapper.writeValueAsString(enable))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    private void freeMerchant(long id) throws Exception {
        //禁用商户
        boolean enable = false;
        mockMvc.perform(put("/merchant/" + id)
                .content(objectMapper.writeValueAsString(enable))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
