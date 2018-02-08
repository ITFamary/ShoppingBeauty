package com.ming.shopping.beauty.service.service;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author CJ
 */
public interface StagingService {
    @Transactional
    void initStagingEnv();
}
