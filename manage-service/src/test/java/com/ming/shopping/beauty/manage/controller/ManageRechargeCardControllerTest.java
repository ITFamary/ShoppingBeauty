package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.ManageConfigTest;
import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.repository.RechargeCardRepository;
import me.jiangcai.lib.sys.service.SystemStringService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author lxf
 */
public class ManageRechargeCardControllerTest extends ManageConfigTest{

    @Autowired
    private RechargeCardRepository rechargeCardRepository;
    @Autowired
    private SystemStringService systemStringService;

    private final String BASE_URI = "/recharge";
    @Test
    public void go()throws Exception{
        Integer defaultAmount = systemStringService.getCustomSystemString("shopping.service.card.amount", null, true, Integer.class, 500);

        Login manage = mockRoot();
        updateAllRunWith(manage);
        final Integer num = 10;
        Login guide = mockLogin();
        //批量生成充值卡
        mockMvc.perform(post(BASE_URI+"/"+guide.getId())
                .content(objectMapper.writeValueAsString(num))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        //应该是10个
        List<RechargeCard> all = rechargeCardRepository.findAll();
        assertThat((all.size() == num)).isTrue();
        for (RechargeCard r : all) {
            assertThat(r.getAmount()).isEqualTo(BigDecimal.valueOf(defaultAmount));
            assertThat(r.getGuideUser().getId()).isEqualTo(guide.getId());
            assertThat(r.getManager().getId()).isEqualTo(manage.getId());
            //看下生成的Code
            System.out.println(r.getCode());
        }


    }
}
