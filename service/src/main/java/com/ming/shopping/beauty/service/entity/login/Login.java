package com.ming.shopping.beauty.service.entity.login;

import lombok.Getter;
import lombok.Setter;
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

    @Column(length = 30)
    private String loginName;

    @Column
    private String password;

    @Column
    private String nickName;

    @Column(columnDefinition = DATE_COLUMN_DEFINITION)
    private LocalDateTime createTime;

    private boolean enabled = true;

    private boolean delete;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptySet();
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

    public boolean isManageable() {
        return false;
    }
}
