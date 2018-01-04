package com.ming.shopping.beauty.service.entity.login;

import lombok.Getter;
import lombok.Setter;
import me.jiangcai.wx.standard.entity.StandardWeixinUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

import static com.ming.shopping.beauty.service.utils.Constant.DATE_COLUMN_DEFINITION;

/**
 * 可登录的角色
 * Created by helloztt on 2017/12/26.
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Setter
@Getter
@Inheritance(strategy = InheritanceType.JOINED)
public class Login implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 这个身份所关联的用户，通常应该是唯一的
     */
    @OneToOne
    private StandardWeixinUser wechatUser;

    @Column(length = 30)
    private String loginName;

    @Column
    private String nickName;
    /**
     * 可推荐
     */
    private boolean guidable;
    /**
     * 可能是个商户或商户管理员
     */
    @OneToOne
    private Merchant merchant;
    /**
     * 可能是个门店或门店管理员
     */
    @OneToOne
    private Store store;
    /**
     * 可能是个用户
     */
    @OneToOne
    private User user;

    @Column(columnDefinition = DATE_COLUMN_DEFINITION)
    private LocalDateTime createTime;

    private boolean enabled = true;

    private boolean delete;
    /**
     * 是否是个超级管理员
     */
    private boolean manageable;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptySet();
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return loginName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !delete;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
