package com.ming.shopping.beauty.service.entity.order;

import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.utils.Constant;
import lombok.Getter;
import lombok.Setter;
import me.jiangcai.payment.PayableOrder;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充值订单
 * @author helloztt
 */
@Entity
@Setter
@Getter
public class RechargeOrder implements PayableOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;
    /**
     * 付款用户
     */
    @ManyToOne
    private User payer;

    @Column(scale = Constant.FLOAT_COLUMN_SCALE,precision = Constant.FLOAT_COLUMN_PRECISION)
    private BigDecimal amount;

    @Column(columnDefinition = Constant.DATE_COLUMN_DEFINITION)
    private LocalDateTime createTime;

    @Override
    public Serializable getPayableOrderId() {
        return "recharge-" + getOrderId();
    }

    @Override
    public BigDecimal getOrderDueAmount() {
        return amount;
    }

    @Override
    public String getOrderProductName() {
        return null;
    }

    @Override
    public String getOrderBody() {
        return "充值";
    }

    @Override
    public String getOrderProductModel() {
        return "充值";
    }

    @Override
    public String getOrderProductCode() {
        return null;
    }

    @Override
    public String getOrderProductBrand() {
        return null;
    }

    @Override
    public String getOrderedName() {
        return payer.getFamilyName();
    }

    @Override
    public String getOrderedMobile() {
        return payer.getLogin().getLoginName();
    }
}
