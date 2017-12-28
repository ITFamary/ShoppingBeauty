package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Manager;
import com.ming.shopping.beauty.service.repository.LoginRepository;
import com.ming.shopping.beauty.service.repository.ManagerRepository;
import com.ming.shopping.beauty.service.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author lxf
 */
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private LoginRepository loginRepository;
    @Autowired
    private ManagerRepository managerRepository;

    @Override
    public List<Manager> managers() { return managerRepository.findAll(); }

    @Override
    public Login get(long id) { return loginRepository.getOne(id); }

    @Override
    public <T extends Login> T newLogin(Class<T> type, String username, String rawPassword) {
        T login;
        try {
            login = type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        login.setCreateTime(LocalDateTime.now());
        return password(login,username,rawPassword);
    }

    @Override
    public <T extends Login> T password(T login, String loginName, String rawPassword) {
        if(loginName != null){
            login.setLoginName(loginName);
        }
        login.setPassword(rawPassword);
        return loginRepository.save(login);
    }

    @Override
    public Login byLoginName(String loginName) { return loginRepository.findByLoginName(loginName); }

    @Override
    public boolean isManager(String loginName) {
        Login login = byLoginName(loginName);
        return login != null && login.isManageable();
    }
}
