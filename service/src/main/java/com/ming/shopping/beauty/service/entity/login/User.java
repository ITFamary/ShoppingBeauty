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
    @ManyToOne
    private Login login;
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
    private User guideUser;
}
