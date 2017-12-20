package com.it.shopping.beauty.service.repository;

import com.it.shopping.beauty.service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by helloztt on 2017/12/21.
 */
public interface UserRepository extends JpaRepository<User,Long>{
}
