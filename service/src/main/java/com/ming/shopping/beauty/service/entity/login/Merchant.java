package com.ming.shopping.beauty.service.entity.login;

import lombok.Getter;
import lombok.Setter;
import me.jiangcai.crud.CrudFriendly;
import me.jiangcai.jpa.entity.support.Address;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.ming.shopping.beauty.service.utils.Constant.DATE_COLUMN_DEFINITION;

/**
 * 商户
 *
 * @author lxf
 */
@Entity
@Getter
@Setter
public class Merchant implements CrudFriendly<Long> {
    @Id
    private Long id;
    /**
     * share primary key
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH,CascadeType.MERGE,CascadeType.PERSIST}, optional = false)
    @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
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
     * TODO 要用新的一套
     */
    private Address address;
    /**
     * 是否是个超级管理员
     */
    private boolean manageable;
    /**
     * 冻结或删除都应设置为 false
     */
    private boolean enabled = true;

    /**
     * 商户拥有的门店.
     */
    @OneToMany
    @OrderBy("createTime desc")
    private List<Store> Stores;

    @Column(columnDefinition = DATE_COLUMN_DEFINITION)
    private LocalDateTime createTime;

    /**
     * 商户是否可用
     *
     * @return
     */
    public boolean isMerchantUsable() {
        return (manageable && enabled)
                || (!manageable && merchant.enabled);
    }

    /**
     * 获取商户ID
     * @return
     */
    public long getMerchantId(){
        return manageable ? id : merchant.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Merchant)) return false;
        Merchant merchant = (Merchant) o;
        return Objects.equals(id, merchant.id) &&
                Objects.equals(manageable, merchant.isMerchantUsable());
    }
}
