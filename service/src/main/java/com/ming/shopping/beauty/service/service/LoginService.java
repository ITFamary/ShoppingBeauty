package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Manager;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author lxf
 */
public interface LoginService {
    /**
     * @return 所有管理员
     */
    @Transactional(readOnly = true)
    List<Manager> managers();

    /**
     * @param id
     * @return 根据id获取某个用户
     */
    Login get(long id);

    /**
     * 新增普通登录
     * @param type        类型
     * @param username    登录名
     * @param rawPassword 明文密码
     * @return 新增身份
     */
    @Transactional
    <T extends Login> T newLogin(Class<T> type, String username, String rawPassword);

    /**
     * 更新密码
     *
     * @param login       一个登录
     * @param rawPassword 明文密码
     * @return 已被保存的登录
     */
    @Transactional
    default <T extends Login> T password(T login, String rawPassword) {
        return password(login, null, rawPassword);
    }

    /**
     * 更新密码
     *
     * @param login       一个登录
     * @param loginName   可选的新登录名；只有非null才会去应用
     * @param rawPassword 明文密码  @return 已被保存的登录
     */
    @Transactional
    <T extends Login> T password(T login, String loginName, String rawPassword);

    /**
     * @param loginName 登录名
     * @return null or 身份
     */
    @Transactional(readOnly = true)
    Login byLoginName(String loginName);

    /**
     * @param loginName 登录名
     * @return 是否为管理员
     */
    @Transactional(readOnly = true)
    boolean isManager(String loginName);
}
