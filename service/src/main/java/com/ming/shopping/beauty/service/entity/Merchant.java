package com.ming.shopping.beauty.service.entity;

import lombok.Getter;
import lombok.Setter;
import me.jiangcai.wx.standard.entity.StandardWeixinUser;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author lxf
 */
@Entity
@Getter
@Setter
public class Merchant {

    /**
     * 用户编号
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "merchant_id")
    private Long merchantId;

    /**
     * 商户名
     */
    @Column(name = "merchant_name")
    private String merchantName;

    /**
     * 密码
     */
    private String password;

    /**
     * 联系方式
     */
    @OneToOne(cascade = CascadeType.ALL)
    private ContactWay contactWay;

    /**
     * 商户拥有的门店.
     */
    @OneToMany
    @OrderBy("createTime desc")
    private List<Store> Stores;

    /**
     * 是否启用
     */
    private boolean enable = true;

    /**
     * 添加时间
     */
    @Column(columnDefinition = "timestamp")
    private LocalDateTime createTime;

    @Override
    public String toString() {
        return "Merchant{" +
                ", merchantName='" + merchantName + '\'' +
                ", contactWay=" + contactWay +
                ", enable=" + enable +
                '}';
    }
}
