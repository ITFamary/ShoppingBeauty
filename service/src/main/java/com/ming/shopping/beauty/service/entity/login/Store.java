package com.ming.shopping.beauty.service.entity.login;

import lombok.Getter;
import lombok.Setter;
import me.jiangcai.jpa.entity.support.Address;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import static com.ming.shopping.beauty.service.utils.Constant.DATE_COLUMN_DEFINITION;

/**
 * 门店
 *
 * @author lxf
 */
@Entity
@Getter
@Setter
public class Store {

    @Id
    private Long id;
    /**
     * share primary key
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.PERSIST}, optional = false)
    @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
    private Login login;
    /**
     * 所属门店
     */
    private Store store;
    /**
     * 门店名称
     */
    @Column(length = 50)
    private String name;
    /**
     * 门店电话
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
     * 是否是个超级管理员
     */
    private boolean manageable;

    /**
     * 门店代表
     */
    @OneToMany
    private List<Represent> represents;

    @Column(columnDefinition = DATE_COLUMN_DEFINITION)
    private LocalDateTime createTime;
    /**
     * 冻结或删除都应设置为 false
     */
    private boolean enabled = true;

    /**
     * 门店是否可用
     */
    public boolean isStoreUsable() {
        return (manageable && enabled)
                || (!manageable && store.enabled);
    }
}
