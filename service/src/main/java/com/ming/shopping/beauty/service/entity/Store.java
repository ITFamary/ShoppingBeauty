package com.ming.shopping.beauty.service.entity;

import com.sun.jndi.cosnaming.IiopUrl;
import lombok.Getter;
import lombok.Setter;
import me.jiangcai.jpa.entity.support.Address;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 门店
 * @author lxf
 */
@Entity
@Getter
@Setter
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 门店名
     */
    @Column(name = "store_name")
    private String storeName;

    /**
     * 门店电话.
     */
    @Column(length = 20)
    private String telephone;
    @ManyToOne
    private Merchant merchant;
    /**
     * 是否启用
     */
    private boolean enable = true;

    /**
     * 门店地址
     */
    private Address address;

    /**
     * 门店代表
     * TODO 我还不太清楚是什么.
     */
    @OneToMany
    private List<Represent> represents;

    /**
     * 添加时间
     */
    @Column(columnDefinition = "timestamp")
    private LocalDateTime createTime;
}
