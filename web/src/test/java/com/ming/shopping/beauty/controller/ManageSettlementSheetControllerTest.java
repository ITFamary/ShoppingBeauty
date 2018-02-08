package com.ming.shopping.beauty.controller;

import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Represent;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.settlement.SettlementSheet;
import com.ming.shopping.beauty.service.entity.support.SettlementStatus;
import com.ming.shopping.beauty.service.model.request.DepositBody;
import com.ming.shopping.beauty.service.model.request.SheetReviewBody;
import com.ming.shopping.beauty.service.repository.MainOrderRepository;
import com.ming.shopping.beauty.service.repository.RechargeCardRepository;
import com.ming.shopping.beauty.service.repository.settlementSheet.SettlementSheetRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ManageSettlementSheetControllerTest extends TogetherTest {

    @Autowired
    private MainOrderRepository mainOrderRepository;
    @Autowired
    private RechargeCardRepository rechargeCardRepository;
    @Autowired
    private SettlementSheetRepository settlementSheetRepository;

    @Test
    public void go() throws Exception {
        //下单者
        Login login = mockLogin();
        //商户
        Merchant merchant = mockMerchant();
        //门店
        Store store = mockStore(merchant);
        for (int i = 0; i < 5; i++) {
            Item item = mockItem(merchant);
            mockStoreItem(store, item);
        }
        //门店代表
        Represent mockRepresent = mockRepresent(store);
        //以门店身份登录
        updateAllRunWith(mockRepresent.getLogin());

        //创建订单
        MainOrder mainOrder = mockMainOrder(login.getUser(), mockRepresent);

        //支付订单
//        /payment/{orderId}

        //余额是0的情况下
        mockMvc.perform(put("/capital/payment/{orderId}", mainOrder.getOrderId())
                .content(objectMapper.writeValueAsString(mainOrder.getFinalAmount()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        //随便写了个充值金额,这里大于设定的默认值 5000
        String contentAsString = mockMvc.perform(put("/capital/payment/{orderId}", mainOrder.getOrderId())
                .content(objectMapper.writeValueAsString(BigDecimal.valueOf(10000)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andReturn().getResponse().getContentAsString();
        assertThat(new BigDecimal(contentAsString)).isEqualTo(new BigDecimal("10000"));

        //root运行
        Login root = mockRoot();
        updateAllRunWith(root);
        //生成充值卡
        mockMvc.perform(post("/recharge" + "/" + mockRepresent.getId())
                .content(objectMapper.writeValueAsString(1))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
        List<RechargeCard> listCard = rechargeCardRepository.findAll();
        RechargeCard rechargeCard = listCard.get(0);


        //充值卡充值
        //以充值人运行
        updateAllRunWith(login);

        DepositBody depositBody = new DepositBody();
        depositBody.setCdKey(rechargeCard.getCode());

        mockMvc.perform(post("/capital/deposit")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("cdKey", depositBody.getCdKey()))
                .andDo(print())
                .andExpect(status().isFound());

        updateAllRunWith(merchant.getLogin());
        //看看他的余额
        String balanceString = mockMvc.perform(get("/login/{id}/balance", login.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        //充值卡是500 余额应该是500
        assertThat(new BigDecimal(balanceString)).isEqualTo(new BigDecimal("500"));

        //重新支付订单
        String contentAsString1 = mockMvc.perform(put("/capital/payment/{orderId}", mainOrder.getOrderId())
                .content(objectMapper.writeValueAsString(BigDecimal.valueOf(10000)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andReturn().getResponse().getContentAsString();
        //余额500 支付10000 应该差9500
        assertThat(new BigDecimal(contentAsString1)).isEqualTo(new BigDecimal("9500"));

        //手动给他充10000
        updateAllRunWith(root);

        Map<String, Object> postData = new HashMap<>();
        postData.put("mobile",login.getLoginName());
        BigDecimal b10000 = new BigDecimal("10000.00");
        postData.put("amount",b10000);
        mockMvc.perform(post("/manage/manualRecharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postData)))
                .andDo(print())
                .andExpect(status().isOk());

        updateAllRunWith(login);
        //再支付订单就ok了
        mockMvc.perform(put("/capital/payment/{orderId}", mainOrder.getOrderId())
                .content(objectMapper.writeValueAsString(mainOrder.getFinalAmount()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isAccepted());

        updateAllRunWith(merchant.getLogin());

        //看看他的余额
        String newBalanceString = mockMvc.perform(get("/login/{id}/balance", login.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        //总金额10500, 支付金额 mainOrder.getFinalAmount()
        assertThat(new BigDecimal(newBalanceString)).isEqualTo(new BigDecimal("10500").subtract(mainOrder.getFinalAmount()));

        //作弊将这个订单完成时间换成一周之前.好生成结算单
        MainOrder newMainOrder = mainOrderRepository.findOne(mainOrder.getOrderId());
        newMainOrder.setPayTime(LocalDateTime.now().minusDays(8));
        mainOrderRepository.save(newMainOrder);

        //再来一单
        MainOrder mainOrderTwo = mockMainOrder(login.getUser(), mockRepresent);
        mockMvc.perform(put("/capital/payment/{orderId}", mainOrderTwo.getOrderId())
                .content(objectMapper.writeValueAsString(mainOrderTwo.getFinalAmount()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isAccepted());
        
        MainOrder newMainOrderTwo = mainOrderRepository.findOne(mainOrderTwo.getOrderId());
        newMainOrderTwo.setPayTime(LocalDateTime.now().minusDays(8));
        mainOrderRepository.save(newMainOrderTwo);
        
        //生成结算单
        mockMvc.perform(post("/settlementSheet/{merchantId}",merchant.getId()))
                .andDo(print())
                .andExpect(status().isCreated()).andReturn().getResponse();

        //应该只有一个结算单
        List<SettlementSheet> all = settlementSheetRepository.findAll();
        assertThat(all.size()).isEqualTo(1);
        SettlementSheet settlementSheet = all.get(0);

        updateAllRunWith(root);
        //查看结算单列表
        String contentAsString2 = mockMvc.perform(get("/settlementSheet"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String sumAmount = objectMapper.readTree(contentAsString2).get("list").get(0).get("settlementAmount").asText();
        BigDecimal bSumAmount = new BigDecimal(sumAmount);
        //偷个懒
        assertThat(bSumAmount.setScale(1))
                .isEqualTo(mainOrder.getSettlementAmount().add(mainOrderTwo.getSettlementAmount()).setScale(1));

        //商户将结算单提交
        SheetReviewBody srb = new SheetReviewBody();
        srb.setComment("我是一个备注");
        srb.setStatus("TO_AUDIT");

        updateAllRunWith(merchant.getLogin());
        mockMvc.perform(put("/settlementSheet/{id}/statusMerchant",settlementSheet.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(srb)))
                .andDo(print())
                .andExpect(status().isNoContent());

        SettlementSheet one = settlementSheetRepository.findOne(settlementSheet.getId());
        assertThat(one.getComment()).isEqualTo("我是一个备注");
        assertThat(one.getSettlementStatus()).isEqualTo(SettlementStatus.TO_AUDIT);


        updateAllRunWith(root);
        //打回  "打回申请的备注"
        srb.setComment("打回申请的备注");
        srb.setStatus("REJECT");
        mockMvc.perform(put("/settlementSheet/{id}/statusManage",settlementSheet.getId())
                .content(objectMapper.writeValueAsString(srb))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        one = settlementSheetRepository.findOne(settlementSheet.getId());
        assertThat(one.getComment()).isEqualTo("打回申请的备注");
        assertThat(one.getSettlementStatus()).isEqualTo(SettlementStatus.REJECT);


        updateAllRunWith(merchant.getLogin());
        //再次提交  "我是一个备注"
        srb.setComment("我是一个备注");
        srb.setStatus("TO_AUDIT");
        mockMvc.perform(put("/settlementSheet/{id}/statusMerchant",settlementSheet.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(srb)))
                .andDo(print())
                .andExpect(status().isNoContent());

        //同意申请  "同意申请"
        srb.setComment("同意申请");
        srb.setStatus("APPROVAL");
        updateAllRunWith(root);
        mockMvc.perform(put("/settlementSheet/{id}/statusManage",settlementSheet.getId())
                .content(objectMapper.writeValueAsString(srb))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        one = settlementSheetRepository.findOne(settlementSheet.getId());
        assertThat(one.getSettlementStatus()).isEqualTo(SettlementStatus.APPROVAL);

        //线下打款了

        //修改状态以打款
        srb.setStatus("ALREADY_PAID");
        srb.setAmount(mainOrder.getSettlementAmount().add(mainOrderTwo.getSettlementAmount()));
        mockMvc.perform(put("/settlementSheet/{id}/statusManage",settlementSheet.getId())
                .content(objectMapper.writeValueAsString(srb))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        one = settlementSheetRepository.findOne(settlementSheet.getId());
        assertThat(one.getSettlementStatus()).isEqualTo(SettlementStatus.ALREADY_PAID);

        //确认收款
        updateAllRunWith(merchant.getLogin());
        srb.setStatus("COMPLETE");
        //商户确认接收
        mockMvc.perform(put("/settlementSheet/{id}/statusMerchant",settlementSheet.getId())
                .content(objectMapper.writeValueAsString(srb))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        one = settlementSheetRepository.findOne(settlementSheet.getId());
        assertThat(one.getSettlementStatus()).isEqualTo(SettlementStatus.COMPLETE);
    }
}
