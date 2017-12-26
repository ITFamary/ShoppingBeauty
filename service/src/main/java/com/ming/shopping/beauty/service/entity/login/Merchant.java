package com.ming.shopping.beauty.service.entity.login;

import lombok.Getter;
import lombok.Setter;
import me.jiangcai.jpa.entity.support.Address;

import javax.persistence.*;
import java.util.List;

/**
 * @author lxf
 */
@Entity
@Getter
@Setter
public class Merchant extends Login {

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
     * 商户拥有的门店.
     */
    @OneToMany
    @OrderBy("createTime desc")
    private List<Store> Stores;
}
