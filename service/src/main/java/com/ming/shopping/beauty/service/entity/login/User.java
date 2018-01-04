package com.ming.shopping.beauty.service.entity.login;

import com.ming.shopping.beauty.service.utils.Constant;
import lombok.Getter;
import lombok.Setter;
import me.jiangcai.wx.model.Gender;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * 用户
 * Created by helloztt on 2017/12/21.
 */
@Entity
@Getter
@Setter
public class User {
    @Id
    private Long id;
    /**
     * share primary key
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
    @PrimaryKeyJoinColumn(name = "id",referencedColumnName = "id")
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
     * 卡号，如果注册时输入充值卡密的，就把他作为卡号
     * TODO 否则怎么样的？
     */
    @Column(length = 30)
    private String cardNo;
    /**
     * 是否激活（充钱了才算激活）
     */
    private boolean active;
}
