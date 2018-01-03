package com.ming.shopping.beauty.service.entity.item;

import com.ming.shopping.beauty.service.entity.login.Manager;
import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.utils.Constant;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 平台钱包充值卡
 *
 * @author helloztt
 */
@Entity
@Getter
@Setter
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"code"})})
public class RechargeCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 卡密，需要唯一
     * TODO 算法待定
     */
    private String code;
    /**
     * 是否已被兑换
     */
    private boolean used;

    @ManyToOne
    private User user;

    @ManyToOne
    private Manager manager;

    /**
     * 金额
     */
    @Column(precision = Constant.FLOAT_COLUMN_PRECISION)
    private BigDecimal amount;
    /**
     * 生成时间
     */
    @Column(columnDefinition = Constant.DATE_COLUMN_DEFINITION)
    private LocalDateTime createTime;
    /**
     * 兑换时间
     */
    @Column(columnDefinition = Constant.DATE_COLUMN_DEFINITION)
    private LocalDateTime usedTime;
}