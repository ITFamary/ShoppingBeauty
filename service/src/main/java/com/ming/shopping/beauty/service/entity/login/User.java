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
public class User extends Login {
    /**
     * 性别
     */
    private Gender gender;
    /**
     * 当前余额
     */
    @Column(scale = Constant.FLOAT_COLUMN_SCALE, precision = Constant.FLOAT_COLUMN_PRECISION)
    private BigDecimal currentAmount = BigDecimal.ZERO;

    // TODO: 2017/12/26 引导人需求未确定
}
