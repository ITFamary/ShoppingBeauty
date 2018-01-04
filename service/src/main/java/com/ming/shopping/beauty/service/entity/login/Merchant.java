package com.ming.shopping.beauty.service.entity.login;

import lombok.Getter;
import lombok.Setter;
import me.jiangcai.jpa.entity.support.Address;

import javax.persistence.*;
import java.util.List;

/**
 * 商户
 * @author lxf
 */
@Entity
@Getter
@Setter
public class Merchant {
    @Id
    @ManyToOne
    private Login login;
    /**
     * 所属商户
     */
    @ManyToOne
    private Merchant merchant;
    /**
     * 商户名称
     */
    @Column(length = 50)
    private String name;

    /**
     * 联系电话
     */
    @Column(length = 20)
    private String telephone;

    /**
     * 联系人
     */
    @Column(length = 50)
    private String contact;

    /**
     * 地址
     */
    private Address address;
    /**
     * 是否是个超级管理员
     */
    private boolean admin;

    /**
     * 商户拥有的门店.
     */
    @OneToMany
    @OrderBy("createTime desc")
    private List<Store> Stores;
}
