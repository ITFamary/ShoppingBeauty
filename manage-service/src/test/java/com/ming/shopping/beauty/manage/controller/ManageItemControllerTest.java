package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.ManageConfigTest;
import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.model.request.NewItemBody;
import com.ming.shopping.beauty.service.repository.ItemRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author lxf
 */
public class ManageItemControllerTest extends ManageConfigTest {

    @Autowired
    private ItemRepository itemRepository;
    @Test
    public void itemList() throws Exception{
        //首先是不具有管理员权限的人访问,拒绝访问
        //随便来个人只要不是管理员
        Login fackManage = mockLogin();
        //身份运行
        updateAllRunWith(fackManage);
        mockMvc.perform(get("/item"))
                .andDo(print())
                .andExpect(status().isForbidden());
        //root权限
        Login login = mockRoot();

        //添加项目
        //首先需要一个商户
        Merchant merchant = mockMerchant();
        updateAllRunWith(merchant.getLogin());

        NewItemBody ib = new NewItemBody();
        ib.setName("测试项目名称");
        ib.setMerchantId(merchant.getId());
        ib.setThumbnailUrl("/abc/hahahah.jpg");
        ib.setItemType("测试");
        ib.setPrice(BigDecimal.valueOf(188.9));
        ib.setSalesPrice(BigDecimal.valueOf(158.9));
        ib.setCostPrice(BigDecimal.valueOf(158.9));
        ib.setDescription("测试项目");
        ib.setRichDescription("详细的描述了测试项目");

        mockItem(merchant);

        //发送添加请求
        mockMvc.perform(post("/item")
                .content(objectMapper.writeValueAsString(ib))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
        //获取项目列表
        String contentAsString = mockMvc.perform(get("/item"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println(itemRepository.getOne(1l).getName());
        String itemId = objectMapper.readTree(contentAsString).get("list").get(0).get("id").asText();
        Item one = itemRepository.getOne(Long.parseLong(itemId));
        assertThat(one != null).isTrue();

    }
}
