package com.ming.shopping.beauty.service.repository;

import com.ming.shopping.beauty.service.entity.login.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by helloztt on 2018/1/4.
 */
public interface LoginRepository extends JpaRepository<Login, Long>, JpaSpecificationExecutor<Login> {
    /**
     * 更新角色可用状态
     * @param id
     * @param enabled
     * @return
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Modifying(clearAutomatically = true)
    @Query("update Login set enabled = ?2 where id = ?1")
    int updateLoginEnabled(long id, boolean enabled);
}
