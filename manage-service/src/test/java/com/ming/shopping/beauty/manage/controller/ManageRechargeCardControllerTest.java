package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.ManageConfigTest;
import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.repository.RechargeCardRepository;
import com.ming.shopping.beauty.service.service.SystemService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author lxf
 */
public class ManageRechargeCardControllerTest extends ManageConfigTest{

    @Autowired
    private RechargeCardRepository rechargeCardRepository;
    @Autowired
    private SystemService systemService;

    private final String BASE_URI = "/recharge";

    /**
     * 测试生成批量卡片
     */
    @Test
    public void go()throws Exception{
        Integer defaultAmount = systemService.currentCardAmount();

        Login manage = mockRoot();
        updateAllRunWith(manage);
        final long oldTotal = rechargeCardRepository.count();
        final Integer num = 10;
        Login guide = mockGuidableLogin();
        Map<String, Object> data = new HashMap<>();
        data.put("num", num);
        final String email = randomEmailAddress();
        data.put("email", email);
        //批量生成充值卡
        mockMvc.perform(post(BASE_URI+"/"+guide.getId())
                .content(objectMapper.writeValueAsString(data))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        //应该是10个
        assertThat(rechargeCardRepository.count())
                .isEqualTo(oldTotal + num);
        for (RechargeCard r : rechargeCardRepository.findByBatch_EmailAddress(email)) {
            assertThat(r.getAmount()).isEqualTo(BigDecimal.valueOf(defaultAmount));
            assertThat(r.getBatch().getGuideUser().getId()).isEqualTo(guide.getId());
            assertThat(r.getBatch().getManager().getId()).isEqualTo(manage.getId());
            //看下生成的Code
            System.out.println(r.getCode());
        }


    }
}
