package com.ming.shopping.beauty.service.repository;

import com.ming.shopping.beauty.service.entity.log.RechargeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author helloztt
 */
public interface RechargeLogRepository extends JpaRepository<RechargeLog,Long>,JpaSpecificationExecutor<RechargeLog> {
}
