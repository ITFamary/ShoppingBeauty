package com.ming.shopping.beauty.service.entity.login;

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
    @Column(scale = 2, precision = 20)
    private BigDecimal currentAmount = BigDecimal.ZERO;

    // TODO: 2017/12/26 引导人需求未确定
}
