package com.ming.shopping.beauty.service.entity.support;

import com.ming.shopping.beauty.service.entity.login.Login;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 管理员级别
 *
 * @author CJ
 */
public enum ManageLevel {
    root("超级管理员", "ROOT"),
    /**
     * 商户管理员
     */
    merchantRoot("商户管理员", Login.ROLE_MERCHANT_ROOT),
    /**
     * 商户操作员， 审核项目
     */
    merchantManager("商户操作员", Login.ROLE_AUDIT_ITEM),
    /**
     * 门店管理员
     */
    storeRoot("门店管理员", Login.ROLE_STORE_ROOT),
    //将来添加角色,原数据不能删除,只能在这之后向下加.
    ;
    private final String[] roles;
    private final String title;

    ManageLevel(String title, String... roles) {
        this.title = title;
        this.roles = roles;
    }

    public static String roleNameToRole(String role) {
        String role2 = role.toUpperCase(Locale.CHINA);
        if (role2.startsWith("ROLE_"))
            return role2;
        return "ROLE_" + role2;
    }

    /**
     * @return role names
     */
    public String[] roles() {
        return roles;
    }

    public Collection<? extends GrantedAuthority> authorities() {
        return Stream.of(roles)
                .map(ManageLevel::roleNameToRole)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    public String title() {
        return title;
    }
}
