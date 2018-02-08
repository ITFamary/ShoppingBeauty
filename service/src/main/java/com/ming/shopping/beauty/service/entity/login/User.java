package com.ming.shopping.beauty.service.entity.login;

import com.ming.shopping.beauty.service.entity.log.CapitalFlow;
import com.ming.shopping.beauty.service.utils.Constant;
import lombok.Getter;
import lombok.Setter;
import me.jiangcai.wx.model.Gender;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

/**
 * 用户
 * Created by helloztt on 2017/12/21.
 */
@Entity
@Getter
@Setter
public class User {
    public static final int CARD_NO_LEN = 20;
    @Id
    private Long id;
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<CapitalFlow> flows;
    /**
     * share primary key
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
    @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
    private Login login;
    /**
     * 姓
     */
    private String familyName;
    /**
     * 性别
     */
    private Gender gender;
    /**
     * 当前余额
     */
    @Column(scale = Constant.FLOAT_COLUMN_SCALE, precision = Constant.FLOAT_COLUMN_PRECISION)
    private BigDecimal currentAmount = BigDecimal.ZERO;
    /**
     * 推荐人
     */
    @ManyToOne
    private Login guideUser;
    /**
     * 卡号，如果注册时输入充值卡密的，就把他作为卡号；否则调用{@link #makeCardNo()}随机生成
     */
    @Column(length = CARD_NO_LEN)
    private String cardNo;

    /**
     * 是否激活（充钱了才算激活）
     */
    public boolean isActive() {
        return !StringUtils.isEmpty(cardNo);
    }

    public static Expression<BigDecimal> getCurrentBalanceExpr(From<?, User> from, CriteriaBuilder cb) {
        return cb.sum(from.join("flows", JoinType.LEFT).get("changed"));
    }

    public static String makeCardNo() {
        return RandomStringUtils.randomAlphanumeric(CARD_NO_LEN);
    }
}
