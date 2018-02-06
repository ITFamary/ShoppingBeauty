package com.ming.shopping.beauty.service.repository;

import com.ming.shopping.beauty.service.entity.log.CapitalFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author lxf
 */
public interface CapitalFlowRepository extends JpaRepository<CapitalFlow,Long>,JpaSpecificationExecutor<CapitalFlow>{
}
