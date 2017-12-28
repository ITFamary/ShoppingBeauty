package com.ming.shopping.beauty.service.repository;

import com.ming.shopping.beauty.service.entity.login.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author lxf
 */
public interface AbstractLoginRepository<T extends Login> extends JpaRepository<T,Long> , JpaSpecificationExecutor<T> {
    T findByLoginName(String name);
}
