package com.ming.shopping.beauty.service.entity.login;

import lombok.Getter;
import lombok.Setter;
import me.jiangcai.wx.model.Gender;

import javax.persistence.*;

/**
 * 门店代表：其实就是门店的收营员，负责帮用户下单
 * @author lxf
 */
@Entity
@Setter
@Getter
public class Represent {
    @Id
    @ManyToOne
    private Login login;
    /**
     * 性别
     */
    private Gender gender;
}
