package com.ming.shopping.beauty.service.entity.login;

import lombok.Getter;
import lombok.Setter;
import me.jiangcai.jpa.entity.support.Address;

import javax.persistence.*;
import java.util.List;

/**
 * 门店
 * @author lxf
 */
@Entity
@Getter
@Setter
public class Store extends Login {

    /**
     * 门店电话.
     */
    @Column(length = 20)
    private String telephone;

    @ManyToOne
    private Merchant merchant;

    /**
     * 门店地址
     */
    private Address address;

    /**
     * 门店代表
     */
    @OneToMany
    private List<Represent> represents;
}
