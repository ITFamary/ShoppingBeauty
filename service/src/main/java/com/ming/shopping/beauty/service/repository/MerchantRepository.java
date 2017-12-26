package com.ming.shopping.beauty.service.repository;

import com.ming.shopping.beauty.service.entity.login.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantRepository extends JpaRepository<Merchant,Long> {
}
